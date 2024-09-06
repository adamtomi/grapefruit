package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.argument.chain.ArgumentChain;
import grapefruit.command.argument.chain.BoundArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.auth.CommandAuthorizationException;
import grapefruit.command.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.dispatcher.config.DispatcherConfigurer;
import grapefruit.command.dispatcher.input.StringReader;
import grapefruit.command.dispatcher.input.StringReaderImpl;
import grapefruit.command.dispatcher.syntax.CommandSyntaxException;
import grapefruit.command.dispatcher.syntax.DuplicateFlagException;
import grapefruit.command.dispatcher.tree.CommandGraph;
import grapefruit.command.util.FlagGroup;
import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static grapefruit.command.dispatcher.syntax.CommandSyntax.LONG_FLAG_PREFIX;
import static grapefruit.command.dispatcher.syntax.CommandSyntax.SHORT_FLAG_PREFIX;
import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl implements CommandDispatcher {
    private final CommandGraph commandGraph = new CommandGraph();
    private final Registry<Command, ArgumentChain> argumentChains = Registry.create(Registry.DuplicateStrategy.reject());
    private final CommandAuthorizer authorizer;
    private final Registry<Key<?>, ArgumentMapper<?>> argumentMappers;
    private final CommandRegistrationHandler registrationHandler;

    CommandDispatcherImpl(DispatcherConfigurer configurer) {
        requireNonNull(configurer, "configurer");
        this.authorizer = requireNonNull(configurer.authorizer(), "authorizer cannot be null");
        this.argumentMappers = requireNonNull(configurer.argumentMappers(), "argumentMappers cannot be null");
        this.registrationHandler = requireNonNull(configurer.registrationHandler(), "registrationHandler cannot be null");
    }

    @Override
    public void register(Iterable<Command> commands) {
        requireNonNull(commands, "commands cannot be null");
        changeRegistrationState(commands, command -> {
            /*
             * Create the argument chain first. This call will
             * throw an error, if no mapper is available for
             * the supplied mapper key, in which case we don't
             * want to proceed with registration any further.
             */
            ArgumentChain argumentChain = createChain(command);
            // The registartion handler is invoked first
            this.registrationHandler.onRegister(command);
            // If the process wasn't interrupted, insert the command into the tree
            this.commandGraph.insert(command);
            // Cache argument chain
            this.argumentChains.store(command, argumentChain);
        });
    }

    @Override
    public void unregister(Iterable<Command> commands) {
        requireNonNull(commands, "commands cannot be null");
        changeRegistrationState(commands, command -> {
            // The registration handler is invoked first
            this.registrationHandler.onUnregister(command);
            // If the process wasn't interrupted, delete the command from the tree
            this.commandGraph.delete(command);
            // Remove cached argument chain
            this.argumentChains.remove(command);
        });
    }

    private void changeRegistrationState(Iterable<Command> commands, Consumer<Command> handler) {
        for (Command command : commands) {
            try {
                // Run state change handler
                handler.accept(command);
            } catch (CommandRegistrationHandler.Interrupt ignored) {
                // Interrupt was thrown, do nothing
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    // TODO investigate this, not a fan of raw types.
    /* Create an argument chain from a command */
    private ArgumentChain createChain(Command command) {
        List<BoundArgument.Positional<?>> positional = new ArrayList<>();
        List<BoundArgument.Flag<?>> flags = new ArrayList<>();

        for (CommandArgument<?> argument : command.arguments()) {
            // Use #mapperKey instead of #key to extract mappers
            Key<?> mapperKey = argument.mapperKey();
            /*
             * Throw an exception if no argument mapper is bound to
             * the provided key. This should not happen if the
             * dispatcher was configured properly.
             */
            ArgumentMapper mapper = this.argumentMappers.get(mapperKey)
                    .orElseThrow(() -> new IllegalStateException("Could not find argument mapper matching '%s'. Requested by: '%s'.".formatted(
                            mapperKey,
                            argument
                    )));

            if (argument.isFlag()) {
                flags.add((BoundArgument.Flag<?>) argument.bind(mapper));
            } else {
                positional.add((BoundArgument.Positional<?>) argument.bind(mapper));
            }
        }

        return ArgumentChain.create(positional, flags);
    }

    private ArgumentChain needChain(Command command) {
        return this.argumentChains.get(command).orElseThrow(() -> new IllegalStateException(
                "No argument chain was cached for command '%s' ('%s'). The dispatcher if not configured properly.".formatted(
                        command,
                        command.meta().route()
                )
        ));
    }

    @Override
    public void dispatch(CommandContext context, String commandLine) throws CommandException {
        requireNonNull(context, "context cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        // Construct a new reader from user input
        StringReader input = new StringReaderImpl(commandLine, context);
        // Find the command instance to execute
        CommandGraph.SearchResult search = this.commandGraph.search(input);
        if (search instanceof CommandGraph.SearchResult.Failure failure) throw failure.cause();

        // The search was successful, we can extract the command instance
        Command command = ((CommandGraph.SearchResult.Success) search).command();

        // Check permissions
        Optional<String> permission = command.meta().permission();
        boolean mayExecute = permission.map(x -> this.authorizer.authorize(x, context))
                .orElse(true);

        // Throw an exception if the user lacks sufficient permissions
        if (!mayExecute) throw new CommandAuthorizationException(permission.orElseThrow());

        // Save the command instance so that we can retrieve it later if needed
        context.put(InternalContextKeys.COMMAND, command);

        // Retrieve argument chain
        ArgumentChain argumentChain = needChain(command);

        // Parse command arguments and store the results in the current context
        ParseInfo parseInfo = parseArguments(context, input, command, argumentChain);
        // Rethrow captured exception if it exists
        if (parseInfo.capturedException().isPresent()) {
            throw parseInfo.capturedException().orElseThrow();
        }

        // Finally, invoke the command
        command.run(context);
    }

    private ParseInfo parseArguments(CommandContext context, StringReader input, Command command, ArgumentChain argumentChain) {
        ParseInfo parseInfo = new ParseInfo();
        try {
            String arg;
            while ((arg = input.peekSingle()) != null) {
                parseInfo.input(arg);
                // Attempt to parse "arg" into a group of flags
                Optional<FlagGroup> flagGroup = FlagGroup.attemptParse(arg, argumentChain.flag());
                // Successfully parsed at least one flag
                if (flagGroup.isPresent()) {
                    // Read a single argument from the input so that the flag argument's mapper
                    // can parse the next in the list (ensuring that the argument mapper doesn't
                    // process the --flagname input part).
                    input.readSingle();
                    // Process each flag in this group
                    for (BoundArgument.Flag<?> flag : flagGroup.orElseThrow()) {
                        FlagArgument<?> argument = flag.argument();
                        parseInfo.argument(flag);
                        parseInfo.suggestFlagValue(true);

                        if (context.contains(argument.key())) {
                            // This means that the flag is already been set
                            throw new DuplicateFlagException(argument.name());
                        }

                        if (argument.isPresenceFlag()) {
                            @SuppressWarnings("unchecked")
                            Key<Object> key = (Key<Object>) argument.key();
                            /*
                             * Set the value to true, since the flag was set
                             * and presence flags are true if set and false
                             * if not.
                             */
                            context.put(key, true);
                        } else {
                            // Map and store flag value
                            mapAndStoreArgument(context, input, flag);
                        }
                    }

                } else {
                    // Attempt to find the first unconsumed positional command argument
                    BoundArgument.Positional<?> firstPositional = argumentChain.positional()
                            .stream()
                            .filter(x -> !context.contains(x.argument().key()))
                            .findFirst()
                            .orElseThrow(() -> CommandSyntaxException.from(input, command, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS));

                    parseInfo.argument(firstPositional);
                    // Map and store argument value
                    mapAndStoreArgument(context, input, firstPositional);
                }

                /*
                 * Since we got to this point, we can safely assume that there were
                 * no errors during the parsing of arguments in this iteration. Thus
                 * we can call reset() and fill in the result in with new data during
                 * the next iteration.
                 */
                parseInfo.reset();
            }

            /*
             * Validate that all non-flag arguments have been parsed. The reason we
             * only check non-flags is that flags are optional, so omitting them
             * is perfectly valid.
             */
            for (CommandArgument<?> argument : command.arguments()) {
                if (!argument.isFlag() && !context.contains(argument.key())) {
                    throw CommandSyntaxException.from(input, command, CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS);
                }
            }

            // Check if we have consumed all arguments
            if (input.hasNext()) throw CommandSyntaxException.from(input, command, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS);
        } catch (CommandException ex) {
            parseInfo.capturedException(ex);
        }

        return parseInfo;
    }

    // TODO rework argument-key relationship to support named argument mappers
    private void mapAndStoreArgument(CommandContext context, StringReader input, BoundArgument<?, ?> binding) throws CommandException {
        Object mappedValue = binding.mapper().tryMap(context, input);
        @SuppressWarnings("unchecked")
        Key<Object> key = (Key<Object>) binding.argument().key();
        context.put(key, mappedValue);
    }

    @Override
    public List<String> suggestions(CommandContext context, String commandLine) {
        requireNonNull(context, "context cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        // Construct a new reader from user input
        StringReader input = new StringReaderImpl(commandLine, context);
        // Find the command instance to create suggestions for
        CommandGraph.SearchResult searchResult = this.commandGraph.search(input);

        /*
         * No command was matched, so we return the primary and
         * secondary aliases of every single child node belonging
         * to the last successfully matched CommandNode. If no
         * CommandNode was matched, root command aliases will
         * be returned.
         */
        if (searchResult instanceof CommandGraph.SearchResult.Failure failure) return failure.validOptions(true);

        // Command can safely be extracted
        Command command = ((CommandGraph.SearchResult.Success) searchResult).command();
        // Retrieve argument chain
        ArgumentChain argumentChain = needChain(command);

        // Parse command
        ParseInfo parseInfo = parseArguments(context, input, command, argumentChain);
        if (parseInfo.capturedException().isEmpty()) {
            // If we successfully process every single argument, the
            // command is complete, we don't need to suggest anything else.
            return List.of();
        } else {
            // Return a list of suggestions based on the parse result.
            return suggestions(context, parseInfo, input, argumentChain);
        }
    }

    // TODO Proper flag group suggestions
    private List<String> suggestions(
            CommandContext context,
            ParseInfo parseInfo,
            StringReader input,
            ArgumentChain argumentChain
    ) {
        /*
         * First, gather a list of arguments that have not been
         * successfully parsed so far.
         */
        List<BoundArgument.Positional<?>> unseenPositional = argumentChain.positional()
                .stream()
                .filter(x -> !context.contains(x.argument().key()))
                .toList();
        // Store flags separately
        List<BoundArgument.Flag<?>> unseenFlags = argumentChain.flag()
                .stream()
                .filter(x -> !context.contains(x.argument().key()))
                .toList();

        String remaining;
        try {
            remaining = input.readRemaining();
        } catch (CommandException ex) {
            remaining = "";
        }

        String arg = parseInfo.input().orElse(remaining);

        if (arg.isEmpty()) {
            // The space hasn't been pressed yet. So the input is something like:
            // 'some route arg0 arg1'. Don't suggest anything.
            return List.of();
        }

        BoundArgument<?, ?> firstUnseen = unseenPositional.isEmpty()
                ? unseenFlags.get(0)
                : unseenPositional.get(0);

        BoundArgument<?, ?> argToParse = parseInfo.argument().orElse(firstUnseen);

        if (argToParse instanceof BoundArgument.Flag<?>) {
            if (!parseInfo.suggestFlagValue()) {
                return formatFlags(unseenFlags);
            } else {
                return argToParse.mapper().listSuggestions(context, arg);
            }
        } else {
            // Make a mutable copy of the list
            List<String> base = new ArrayList<>(argToParse.mapper().listSuggestions(context, arg));

            // If the current argument starts with '-', we list flags as well
            if (arg.startsWith(SHORT_FLAG_PREFIX)) {
                base.addAll(formatFlags(unseenFlags));
            }

            return base;
        }
    }

    private List<String> formatFlags(Collection<BoundArgument.Flag<?>> flags) {
        return flags.stream()
                .map(BoundArgument::argument)
                .map(x -> List.of("%s%s".formatted(SHORT_FLAG_PREFIX, x.shorthand()), "%s%s".formatted(LONG_FLAG_PREFIX, x.name())))
                .flatMap(Collection::stream)
                .toList();
    }
}
