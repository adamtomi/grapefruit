package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.argument.CommandChainFactory;
import grapefruit.command.argument.DuplicateFlagException;
import grapefruit.command.argument.UnrecognizedFlagException;
import grapefruit.command.dispatcher.config.DispatcherConfig;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.CommandSyntaxException;
import grapefruit.command.tree.CommandGraph;
import grapefruit.command.util.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl<S> implements CommandDispatcher<S> {
    private static final char SHORT_FLAG_PREFIX_CH = '-';
    private final CommandGraph<S> commandGraph = new CommandGraph<>();
    private final CommandChainFactory<S> chainFactory = CommandChain.factory();
    // Store computed CommandChain instances mapped to their respective CommandModule.
    private final Map<CommandModule<S>, CommandChain<S>> computedChains = new HashMap<>();
    /* Configurable properties */
    private final CommandAuthorizer<S> authorizer;
    private final CommandRegistrationHandler<S> registrationHandler;

    CommandDispatcherImpl(final DispatcherConfig<S> config) {
        requireNonNull(config, "config cannot be null");
        this.authorizer = config.authorizer();
        this.registrationHandler = config.registrationHandler();
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
        final CommandContext<S> context = new CommandContextImpl<>(source, requireChain(cmd));

        // 2) Authorize user
        checkPermissions(context);

        // 3) Check command conditions

        // 4) Invoke pre-process listeners

        // 5) Process command
        processCommand(context, input);

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
        return List.of();
    }

    private CommandChain<S> requireChain(final CommandModule<S> command) {
        final CommandChain<S> chain = this.computedChains.get(command);
        if (chain == null) {
            throw new IllegalStateException("No command chain instance has been computed for command %s".formatted(command));
        }

        return chain;
    }

    // TODO only check flag permissions if the flag is set.
    private void checkPermissions(CommandContext<S> context) throws CommandAuthorizationException {
        final CommandChain<S> chain = context.chain();
        final Set<String> lacking = Stream.of(chain.route(), chain.arguments(), chain.flags())
                .flatMap(Collection::stream)
                .map(CommandArgument::permission)
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .filter(x -> !this.authorizer.authorize(context.source(), x))
                .collect(Collectors.toSet());

        if (!lacking.isEmpty()) throw new CommandAuthorizationException(lacking);

        /*
         * perm("a.b.c"); -> PermissionCondition
         *
         * perm("a.b.c").and(isPlayer()); -> CommandDistpatcher$112$lambda$.......
         *
         * for (CommandCondition condition : chain.conditions()) {
         *      if (!condition.test(context)) throw new UnfulfilledConditionException(condition);
         *
         * }
         */
    }

    private static <S> void processCommand(final CommandContext<S> context, final CommandInputTokenizer input) throws CommandException {
        String arg;
        final CommandChain<S> chain = context.chain();
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
                    consumeArgument(required.orElseThrow(), context, input);
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
                for (final CommandArgument.Flag<S, ?> flag : flags) consumeFlag(flag, arg, context, input);
            }
        }

        verifyRequiredArguments(context, chain);
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

    private static <S, T> void consumeFlag(final CommandArgument.Flag<S, T> flag, final String expression, final CommandContext<S> context, final CommandInputTokenizer input) throws CommandException {
        if (context.has(flag.key())) {
            throw DuplicateFlagException.fromInput(input, expression);
        }

        consumeArgument(flag, context, input);
    }

    private static <S, T> void consumeArgument(final CommandArgument.Dynamic<S, T> argument, final CommandContext<S> context, final CommandInputTokenizer input) throws CommandException {
        // 1) Map argument into the correct type. This will throw an exceptioniif
        //    the conversion fails.
        final T result = argument.mapper().tryMap(context, input);
        // 2) Store the result in the current context
        context.store(argument.key(), result);
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
}
