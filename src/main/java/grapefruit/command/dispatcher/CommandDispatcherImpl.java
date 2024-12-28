package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.argument.CommandChainFactory;
import grapefruit.command.argument.DuplicateFlagException;
import grapefruit.command.argument.UnrecognizedFlagException;
import grapefruit.command.argument.condition.CommandCondition;
import grapefruit.command.argument.condition.UnfulfilledConditionException;
import grapefruit.command.argument.mapper.ArgumentMappingException;
import grapefruit.command.completion.Completion;
import grapefruit.command.completion.CompletionSupport;
import grapefruit.command.dispatcher.config.DispatcherConfig;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;
import grapefruit.command.tree.CommandGraph;
import grapefruit.command.tree.NoSuchCommandException;
import grapefruit.command.util.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static grapefruit.command.util.StringUtil.startsWithIgnoreCase;
import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl<S> implements CommandDispatcher<S> {
    private static final char SHORT_FLAG_PREFIX_CH = '-';
    private static final String SHORT_FLAG_PREFIX = String.valueOf(SHORT_FLAG_PREFIX_CH);
    private static final String LONG_FLAG_PREFIX = SHORT_FLAG_PREFIX.repeat(2);
    private final CommandGraph<S> commandGraph = new CommandGraph<>();
    private final CommandChainFactory<S> chainFactory = CommandChain.factory();
    // Store computed CommandChain instances mapped to their respective CommandModule.
    private final Map<CommandModule<S>, CommandChain<S>> computedChains = new HashMap<>();
    /* Configurable properties */
    private final CommandRegistrationHandler<S> registrationHandler;
    private final ContextDecorator<S> contextDecorator;

    CommandDispatcherImpl(final DispatcherConfig<S> config) {
        requireNonNull(config, "config cannot be null");
        this.registrationHandler = config.registrationHandler();
        this.contextDecorator = config.contextDecorator();
    }

    @Override
    public void register(final CommandModule<S> command) {
        requireNonNull(command, "command cannot be null");
        if (this.computedChains.containsKey(command)) {
            throw new IllegalStateException("Command %s has already been registered".formatted(command));
        }

        // Compute command chain instance
        final CommandChain<S> chain = command.chain(this.chainFactory);

        // Skip registration if the handler returns false
        if (!this.registrationHandler.register(chain)) return;

        this.computedChains.put(command, chain);
        this.commandGraph.insert(chain, command);
    }

    @Override
    public void unregister(final CommandModule<S> command) {
        requireNonNull(command, "command cannot be null");

        final CommandChain<S> chain = requireChain(command);

        // Skip unregistration if the handler returns false
        if (!this.registrationHandler.unregister(chain)) return;

        this.computedChains.remove(command, chain);
        this.commandGraph.delete(chain);
    }

    @Override
    public void dispatch(final S source, final String command) throws CommandException {
        requireNonNull(source, "source cannot be null");
        requireNonNull(command, "command cannot be null");

        final CommandInputTokenizer.Internal input = (CommandInputTokenizer.Internal) CommandInputTokenizer.wrap(command);
        final CommandModule<S> cmd = this.commandGraph.query(input);
        final CommandContext<S> context = createContext(source, requireChain(cmd), ContextDecorator.Mode.DISPATCH);
        testRequiredConditions(context);

        final CommandParseResult<S> parseResult = processCommand(context, input);
        parseResult.rethrowCaptured();

        try {
            cmd.execute(context);
        } catch (final Throwable ex) {
            throw new CommandInvocationException(ex);
        }
    }

    @Override
    public List<Completion> complete(final S source, final String command) {
        requireNonNull(source, "source cannot be null");
        requireNonNull(command, "command cannot be null");

        final CommandInputTokenizer.Internal input = (CommandInputTokenizer.Internal) CommandInputTokenizer.wrap(command);
        final CommandModule<S> cmd;
        try {
            cmd = this.commandGraph.query(input);
        } catch (final NoSuchCommandException ex) {
            if (!command.isEmpty() && input.unsafe().lastConsumed().isBlank() && !ex.argument().isBlank()) {
                return List.of();
            }

            return ex.alternatives().stream()
                    .flatMap(x -> Stream.concat(Stream.of(x.name()), x.aliases().stream()))
                    .filter(x -> startsWithIgnoreCase(x, ex.argument()))
                    .map(Completion::completion)
                    .toList();
        } catch (final CommandException ex) {
            return List.of();
        }

        if (!input.canRead() && !input.unwrap().endsWith(" ")) {
            return List.of();
        }

        final CommandContext<S> context = createContext(source, requireChain(cmd), ContextDecorator.Mode.COMPLETE);
        CommandParseResult<S> parseResult = processCommand(context, input);
        final Optional<CommandException> capturedOpt = parseResult.capturedException();

        if (parseResult.isComplete()) {
            return List.of();
        }

        if (capturedOpt.isPresent()) {
            final CommandException ex = capturedOpt.orElseThrow();
            if (ex instanceof DuplicateFlagException) {
                return List.of();
            } else if (ex instanceof UnrecognizedFlagException ufe) {
                final String arg = ufe.argument();
                if (ufe.argument().startsWith(SHORT_FLAG_PREFIX)) {
                    parseResult = parseResult.withInput(arg);
                } else {
                    return List.of();
                }
            } else if (ex instanceof CommandArgumentException) { // This means we couldn't map user input into some type.
                // Move the cursor back for completions.
                input.unsafe().moveTo(parseResult.cursor());
            }
        }

        return collectCompletions(context, input, parseResult);
    }

    private CommandContext<S> createContext(final S source, final CommandChain<S> chain, final ContextDecorator.Mode mode) {
        final CommandContext<S> context = new CommandContextImpl<>(source, chain);
        this.contextDecorator.apply(context, mode);
        return context;
    }

    private CommandChain<S> requireChain(final CommandModule<S> command) {
        final CommandChain<S> chain = this.computedChains.get(command);
        if (chain == null) {
            throw new IllegalStateException("No command chain instance has been computed for command %s".formatted(command));
        }

        return chain;
    }

    // Test conditions of literal and required arguments
    private static <S> void testRequiredConditions(final CommandContext<S> context) throws UnfulfilledConditionException {
        final CommandChain<S> chain = context.chain();
        final List<CommandCondition<S>> conditions = Stream.concat(chain.route().stream(), chain.arguments().stream())
                .map(CommandArgument::condition)
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .toList();

        for (final CommandCondition<S> condition : conditions) condition.test(context);
    }

    private static <S> CommandParseResult<S> processCommand(final CommandContext<S> context, final CommandInputTokenizer.Internal input) {
        String arg;
        final CommandChain<S> chain = context.chain();
        final CommandParseResult.Builder<S> builder = CommandParseResult.createBuilder(chain, input);
        try {
            while ((arg = input.peekWord()) != null) {
                // Attempt to parse arg into a single flag or a group of flags
                final Tuple2<List<CommandArgument.Flag<S, ?>>, Supplier<UnrecognizedFlagException>> flagResult = parseFlagGroup(arg, input, chain.flags());
                if (flagResult.right().isPresent()) {
                    /*
                     * We do this to stay consistent with the rest of the library. If an
                     * argument is inspected and was found to be incorrect, we remove it
                     * from the remaining argument list.
                     */
                    input.readWord();
                    throw flagResult.right().orElseThrow().get();
                }

                final List<CommandArgument.Flag<S, ?>> flags = flagResult.left().orElseThrow();
                // If the list is not empty, we managed to parse into at least one flag
                if (flags.isEmpty()) {
                    // No flags were, matched, we retrieve the first unseen
                    // required argument.
                    final Optional<CommandArgument.Required<S, ?>> required = firstUnseen(chain.arguments(), context);
                    if (required.isPresent()) {
                        consumeArgument(required.orElseThrow(), context, input, builder, arg);
                    } else {
                        /*
                         * At this point, we need to throw an exception to indicate to the
                         * user that no more required arguments need to be passed.
                         *
                         * 1) We either have more flags that can take values, in which
                         *    case we throw an unrecognized flag exception, or
                         *
                         * 2) There could be no more flags, in which case we throw
                         *    a syntax exception with the "TOO_MANY_ARGUMENTS" reason,
                         *    because we can't handle more arguments.
                         */
                        input.readWord(); // Consume the current argument to be inline with the rest of the code
                        throw firstUnseen(chain.flags(), context).isPresent()
                                ? UnrecognizedFlagException.fromInput(input, arg, arg)
                                : new CommandSyntaxException(chain, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS);
                    }

                } else {
                    // Get rid of the flag expression itself
                    input.readWord();
                    // Parse each flag argument
                    for (final CommandArgument.Flag<S, ?> flag : flags) consumeFlag(flag, arg, context, input, builder);
                }
            }

            verifyRequiredArguments(context, chain);
        } catch (final CommandException ex) {
            builder.capture(ex);
        }

        return builder.build();
    }

    private static <S> void verifyRequiredArguments(final CommandContext<S> context, final CommandChain<S> chain) throws CommandSyntaxException {
        /*
         * Verify that all non-flag arguments have been parsed. The reason we
         * only check non-flags is that flags are optional, so omitting them
         * is perfectly valid.
         */
        if (firstUnseen(chain.arguments(), context).isPresent()) {
            throw new CommandSyntaxException(chain, CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS);
        }
    }

    private static <S, C extends CommandArgument.Dynamic<S, ?>> Optional<C> firstUnseen(
            final List<C> arguments,
            final CommandContext<S> context
    ) {
        return arguments.stream().filter(x -> !context.has(x.key())).findFirst();
    }

    private static <S, T> void consumeFlag(
            final CommandArgument.Flag<S, T> flag,
            final String expression,
            final CommandContext<S> context,
            final CommandInputTokenizer.Internal input,
            final CommandParseResult.Builder<S> builder
    ) throws CommandException {
        if (context.has(flag.key())) {
            throw DuplicateFlagException.fromInput(input, expression);
        }

        /*
         * Because flags are optional, we can't test their conditions
         * early like we do with literal and required arguments. So,
         * do the check now.
         */
        final Optional<CommandCondition<S>> condition = flag.condition();
        if (condition.isPresent()) {
            condition.orElseThrow().test(context);
        }

        consumeArgument(flag, context, input, builder, expression);
    }

    private static <S, T> void consumeArgument(
            final CommandArgument.Dynamic<S, T> argument,
            final CommandContext<S> context,
            final CommandInputTokenizer.Internal input,
            final CommandParseResult.Builder<S> builder,
            final String value
    ) throws CommandException {
        try {
            // 1) Mark beginning
            builder.begin(argument, value);
            // 2) Map argument into the correct type. This will throw an exception if
            //    the conversion fails.
            final T result = argument.mapper().tryMap(context, input);
            builder.push(input.unsafe().lastConsumed());
            // 3) Store the result in the current context
            context.store(argument.key(), result);
            // 4) Mark end
            if (input.peek() == ' ') builder.end();
        } catch (final ArgumentMappingException ex) {
          throw input.unsafe().exception(
                  input.unsafe().lastConsumed(),
                  (consumed, arg, remaining) -> new CommandArgumentException(ex, consumed, arg, remaining)
          );
        } catch (final MissingInputException ex) {
            throw new CommandSyntaxException(context.chain(), CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS);
        }
    }

    private static <S> Tuple2<List<CommandArgument.Flag<S, ?>>, Supplier<UnrecognizedFlagException>> parseFlagGroup(
            final String expression,
            final CommandInputTokenizer input,
            final List<CommandArgument.Flag<S, ?>> candidates
    ) {
        /*
         * If the expression isn't even 2 characters long or doesn't start with
         * '-', it's not a flag group.
         */
        if (expression.length() < 2 || expression.charAt(0) != SHORT_FLAG_PREFIX_CH) return new Tuple2<>(List.of(), null);

        /*
         * This means the expression is prefixed with '--' , it's either a long
         * flag name, or '--' literally.
         */
        if (expression.charAt(1) == SHORT_FLAG_PREFIX_CH) {
            // The expression is literally '--' , which is not a valid flag group, returning.
            if (expression.length() == 2) return new Tuple2<>(List.of(), null);

            // Long flag name, extract it
            final String flagName = expression.substring(2);
            // Attempt to find a flag with the extracted name. Return unrecognized flag
            // exception, if none was found.
            final Optional<CommandArgument.Flag<S, ?>> candidate = candidates.stream()
                    .filter(x -> x.name().equals(flagName))
                    .findFirst();

            return candidate.isPresent()
                    ? new Tuple2<>(List.of(candidate.orElseThrow()), null)
                    : new Tuple2<>(null, () -> UnrecognizedFlagException.fromInput(input, expression, flagName));
        } else {
            // We either have a single shorthand or a group of shorthands.
            final char[] shorthands = expression.substring(1).toCharArray();
            final List<CommandArgument.Flag<S, ?>> flags = new ArrayList<>();

            // Find flags by their shorthands
            for (char c : shorthands) {
                /*
                 * Flag shorthands are expected to be alphabetic. If this
                 * argument is not, it isn't a flag shorthand either, thus
                 * we return an empty optional, otherwise we could run into
                 * a problem of trying to interpret negative numbers as
                 * flag shorthands for instance.
                 */
                if (!Character.isAlphabetic(c)) return new Tuple2<>(List.of(), null);

                final Optional<CommandArgument.Flag<S, ?>> candidate = candidates.stream()
                        .filter(x -> x.shorthand() == c)
                        .findFirst();

                if (candidate.isPresent()) {
                    flags.add(candidate.orElseThrow());
                } else {
                    // Return an error if an incorrect shorthand was provided
                    return new Tuple2<>(null, () -> UnrecognizedFlagException.fromInput(input, expression, String.valueOf(c)));
                }
            }

            return new Tuple2<>(List.copyOf(flags), null);
        }
    }

    private static <S> List<Completion> collectCompletions(final CommandContext<S> context, final CommandInputTokenizer input, final CommandParseResult<S> parseResult) {
        final String remaining = input.remainingOrEmpty();
        final boolean completeNext = input.unwrap().endsWith(" ");

        final CommandArgument.Dynamic<S, ?> argument = resolveArgumentToComplete(parseResult);
        final String argToComplete = !remaining.isEmpty()
                ? remaining
                : completeNext ? remaining : parseResult.lastInput().orElse(remaining);

        final List<Completion> base = argument.isFlag()
                ? collectFlagCompletions(context, parseResult, argument.asFlag(), argToComplete)
                : collectArgumentCompletions(context, parseResult, argument, argToComplete);

        return base.stream()
                .filter(x -> startsWithIgnoreCase(x.content(), argToComplete.trim()))
                .toList();
    }

    private static <S> List<Completion> collectFlagCompletions(
            final CommandContext<S> context,
            final CommandParseResult<S> parseResult,
            final CommandArgument.Flag<S, ?> argument,
            final String argToComplete
    ) {
        if (argument.asFlag().isPresence()) {
            return Stream.of(completeFlags(parseResult.remainingFlags()), completeFlagGroup(argToComplete, parseResult.remainingFlags()))
                    .flatMap(Collection::stream)
                    .toList();
        } else {
            /*
             * Decide whether to complete flag names or a flag value. Last input will hold the flag name (or
             * flag group) that was passed by the user. So if the remaining string is the same, we can't complete
             * values yet.
             */
            final boolean completeFlagValue = parseResult.lastInput().map(x -> !x.equals(argToComplete)).orElse(false);
            final List<Completion> base = new ArrayList<>();

            if (completeFlagValue) {
                base.addAll(argument.mapper().complete(context, argToComplete));
            } else {
                if (parseResult.lastInput().map(String::isEmpty).orElse(true)) {
                    base.addAll(completeFlags(parseResult.remainingFlags()));
                }

                base.addAll(completeFlagGroup(argToComplete, parseResult.remainingFlags()));
            }

            return base;
        }
    }

    private static <S> List<Completion> collectArgumentCompletions(
            final CommandContext<S> context,
            final CommandParseResult<S> parseResult,
            final CommandArgument.Dynamic<S, ?> argument,
            final String argToComplete
    ) {
        return Stream.of(argument.mapper().complete(context, argToComplete), completeFlags(parseResult.remainingFlags()))
                .flatMap(Collection::stream)
                .toList();
    }

    private static <S> CommandArgument.Dynamic<S, ?> resolveArgumentToComplete(final CommandParseResult<S> parseResult) {
        final Optional<CommandArgument.Dynamic<S, ?>> lastArgument = parseResult.lastArgument();
        if (lastArgument.isPresent()) {
            return lastArgument.orElseThrow();
        }

        final List<CommandArgument.Required<S, ?>> remainingArgs = parseResult.remainingArguments();
        final List<CommandArgument.Flag<S, ?>> remainingFlags = parseResult.remainingFlags();

        if (parseResult.isComplete()) {
            throw new IllegalStateException("No arguments are left to complete.");
        }

        return (remainingArgs.isEmpty() ? remainingFlags : remainingArgs).getFirst();
    }

    private static <S> List<Completion> completeFlag(final CommandArgument.Flag<S, ?> flag) {
        final List<String> result = new ArrayList<>();
        result.add(LONG_FLAG_PREFIX + flag.name());

        if (flag.shorthand() != 0) result.add(SHORT_FLAG_PREFIX + flag.shorthand());

        return CompletionSupport.strings(result);
    }

    private static <S> List<Completion> completeFlags(final Collection<CommandArgument.Flag<S, ?>> flags) {
        return flags.stream()
                .map(CommandDispatcherImpl::completeFlag)
                .flatMap(Collection::stream)
                .toList();
    }

    private static <S> List<Completion> completeFlagGroup(final String argument, final List<CommandArgument.Flag<S, ?>> flags) {
        final List<String> result = new ArrayList<>();
        // If charAt(1) is not alphabetic, this is not a flag group. This is to prevent
        // interpreting flag names (--flag-name) as flag groups.
        if (argument.length() > 1 && argument.charAt(0) == SHORT_FLAG_PREFIX_CH && Character.isAlphabetic(argument.charAt(1))) {
            for (final CommandArgument.Flag<S, ?> flag : flags) {
                // If we don't have a valid shorthand, or it is already in 'argument', ignore this flag
                if (flag.shorthand() == 0 || argument.indexOf(flag.shorthand()) != -1) continue;

                result.add(argument + flag.shorthand());
            }
        }

        return CompletionSupport.strings(result);
    }
}
