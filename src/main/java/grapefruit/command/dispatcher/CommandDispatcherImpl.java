package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.argument.CommandChainFactory;
import grapefruit.command.argument.DuplicateFlagException;
import grapefruit.command.argument.UnrecognizedFlagException;
import grapefruit.command.argument.condition.CommandCondition;
import grapefruit.command.argument.condition.UnfulfilledConditionException;
import grapefruit.command.dispatcher.config.DispatcherConfig;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.CommandSyntaxException;
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

        final CommandInputTokenizer input = CommandInputTokenizer.wrap(command);
        // 1) Find corresponding command module
        final CommandModule<S> cmd = this.commandGraph.search(input);
        final CommandContext<S> context = createContext(source, requireChain(cmd), ContextDecorator.Mode.DISPATCH);

        // 2) Authorize user
        testRequiredConditions(context);

        // 3) Check command conditions

        // 4) Invoke pre-process listeners

        // 5) Process command
        final CommandParseResult<S> parseResult = processCommand(context, input);
        parseResult.rethrowCaptured();

        // 6) Invoke pre-execution listeners

        // 7) Execute command
        try {
            cmd.execute(context);
        } catch (final Throwable ex) {
            throw new CommandInvocationException(ex);
        }

        // 8) Invoke post-execution listeners
    }

    @Override
    public List<String> complete(final S source, final String command) {
        requireNonNull(source, "source cannot be null");
        requireNonNull(command, "command cannot be null");

        System.out.println("====================== COMPLETE ======================");
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(command);
        final CommandModule<S> cmd;
        try {
            cmd = this.commandGraph.search(input);
        } catch (final NoSuchCommandException ex) {
            System.out.println("nosuchcommand: %s".formatted(ex.argument()));
            if (input.unwrap().endsWith(" ")) {
                System.out.println("space");
                return List.of();
            }

            return ex.alternatives().stream()
                    .flatMap(x -> Stream.concat(Stream.of(x.name()), x.aliases().stream()))
                    // TODO fix this.
                    // .filter(x -> startsWithIgnoreCase(ex.argument(), x))
                    .toList();
        } catch (final CommandException ex) {
            System.out.println("generic cmd exception 1");
            // TODO
            return List.of();
        }

        if (!input.hasNext() && !input.unwrap().endsWith(" ")) {
            System.out.println("No space && !hasNext");
            return List.of();
        }

        final CommandContext<S> context = createContext(source, requireChain(cmd), ContextDecorator.Mode.COMPLETE);
        final CommandParseResult<S> parseResult = processCommand(context, input);
        final Optional<CommandException> capturedOpt = parseResult.capturedException();

        if (capturedOpt.isPresent()) {
            System.out.println("captured ex is present");
            final CommandException ex = capturedOpt.orElseThrow();
            System.out.println(ex.getClass());
            if (ex instanceof DuplicateFlagException) {
                System.out.println("duplicate flag");
                return List.of();
            } else if (ex instanceof UnrecognizedFlagException ufe) {
                if (ufe.argument().startsWith(SHORT_FLAG_PREFIX)) {
                    System.out.println("looks like a flag prefix, arg is " + ufe.argument());
                    System.out.println("updating last input");
                    parseResult.setLastInput(ufe.argument());
                } else {
                    System.out.println("Unrecognized flag, returning empty list");
                    return List.of();
                }
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

    private static <S> CommandParseResult<S> processCommand(final CommandContext<S> context, final CommandInputTokenizer input) {
        String arg;
        final CommandChain<S> chain = context.chain();
        final CommandParseResult.Builder<S> builder = CommandParseResult.createBuilder(chain);
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
            final CommandInputTokenizer input,
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
            final CommandInputTokenizer input,
            final CommandParseResult.Builder<S> builder,
            final String arg
    ) throws CommandException {
        // 1) Mark beginning
        builder.begin(argument, arg);
        // 2) Map argument into the correct type. This will throw an exceptioniif
        //    the conversion fails.
        final T result = argument.mapper().tryMap(context, input);
        // 3) Store the result in the current context
        context.store(argument.key(), result);
        // 4) Mark end
        // TODO we'd need to get acccess to the actual argument that was consumed by the mapper (greedy, quotable string mappers)
        // TODO this approach might be fucked.
        if (input.peek() == ' ' || input.peek() == 0) builder.end();
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

    private static <S> List<String> collectCompletions(final CommandContext<S> context, final CommandInputTokenizer input, final CommandParseResult<S> parseResult) {
        System.out.println(parseResult);
        final String remaining = input.remainingOrEmpty();
        final boolean completeNext = remaining.endsWith(" ");

        System.out.println("Remaining: '%s'".formatted(remaining));
        System.out.println("completeNext: '%s'".formatted(completeNext));

        final CommandArgument.Dynamic<S, ?> firstUnseen = parseResult.remainingArguments().isEmpty()
                ? parseResult.remainingFlags().getFirst()
                : parseResult.remainingArguments().getFirst();

        final CommandArgument.Dynamic<S, ?> selectedArgument = parseResult.lastArgument().orElse(firstUnseen);
        final String selectedInput = completeNext ? remaining : parseResult.lastInput().orElse(remaining);

        System.out.println("firstUnseen: '%s'".formatted(firstUnseen));
        System.out.println("selectedArgument: '%s'".formatted(selectedArgument));
        System.out.println("selectedInput: '%s'".formatted(selectedInput));

        final List<String> base = new ArrayList<>();

        if (selectedArgument.isFlag()) {
            System.out.println("This is a flag");
            if (selectedArgument.asFlag().isPresence()) { //  && completeNext
                // TODO well this is fucked
                // TODO Why are we taking firstUnseen.asFlag? shouldn't this be selectedArgument.asFlag?
                System.out.println("Presence flag && completeNext, completing flags");
                // base.addAll(completeFlag(firstUnseen.asFlag()));
                base.addAll(completeFlags(parseResult.remainingFlags()));
            } else {
                System.out.println("Not a presence flag"); // , or completeNext is false
                // TODO only include these suggestions, if a) completeNext is true or b) the flag name (or group) has been completed already
                if (!completeNext) {
                    System.out.println("Including flag group completions");
                    base.addAll(completeFlagGroup(selectedInput, parseResult.remainingFlags()));
                }

                System.out.println("Including suggestions from mapper");
                final String arg = input.remainingOrEmpty();
                System.out.println("Completing for: '%s'".formatted(arg));
                base.addAll(selectedArgument.mapper().complete(context, arg));
            }
        } else {
            System.out.println("not a flag, adding mapper suggestions");
            base.addAll(selectedArgument.mapper().complete(context, selectedInput));

            if (!selectedInput.isEmpty() && selectedInput.charAt(0) == SHORT_FLAG_PREFIX_CH) {
                System.out.println("looks like the beginning of a flag/flag group, adding flag suggestions");
                base.addAll(completeFlags(parseResult.remainingFlags()));
            }
        }

        System.out.println("returning...");
        return base.stream()
                // .filter(x -> startsWithIgnoreCase(x, selectedInput.trim()))
                .toList();
    }

    private static <S> List<String> completeFlag(final CommandArgument.Flag<S, ?> flag) {
        final List<String> result = new ArrayList<>();
        result.add(LONG_FLAG_PREFIX + flag.name());

        if (flag.shorthand() != 0) result.add(SHORT_FLAG_PREFIX + flag.shorthand());

        return result;
    }

    private static <S> List<String> completeFlags(final Collection<CommandArgument.Flag<S, ?>> flags) {
        return flags.stream()
                .map(CommandDispatcherImpl::completeFlag)
                .flatMap(Collection::stream)
                .toList();
    }

    private static <S> List<String> completeFlagGroup(final String argument, final List<CommandArgument.Flag<S, ?>> flags) {
        final List<String> result = new ArrayList<>();
        // If charAt(0) is not alphabetic, this is not a flag group.
        if (argument.length() > 1 && Character.isAlphabetic(argument.charAt(1))) {
            System.out.println("Including flag group completions");
            for (final CommandArgument.Flag<S, ?> flag : flags) {
                // If we don't have a valid shorthand, or it is already in 'argument', ignore this flag
                if (flag.shorthand() == 0 || argument.indexOf(flag.shorthand()) != -1) continue;

                result.add(argument + flag.shorthand());
            }
        }

        return result;
    }
}
