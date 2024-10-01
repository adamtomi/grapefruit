package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.binding.BoundArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.argument.modifier.ArgumentModifier;
import grapefruit.command.dispatcher.auth.CommandAuthorizationException;
import grapefruit.command.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.dispatcher.condition.CommandCondition;
import grapefruit.command.dispatcher.condition.UnfulfilledConditionException;
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
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

import static grapefruit.command.dispatcher.syntax.CommandSyntax.LONG_FLAG_FORMAT;
import static grapefruit.command.dispatcher.syntax.CommandSyntax.SHORT_FLAG_FORMAT;
import static grapefruit.command.dispatcher.syntax.CommandSyntax.SHORT_FLAG_PREFIX;
import static grapefruit.command.util.StringUtil.startsWithIgnoreCase;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.partitioningBy;

final class CommandDispatcherImpl implements CommandDispatcher {
    private final CommandGraph commandGraph = new CommandGraph();
    private final Registry<Command, CommandInfo> knownCommands = Registry.create(Registry.DuplicateStrategy.reject());
    private final CommandAuthorizer authorizer;
    private final Registry<Key<?>, ArgumentMapper<?>> argumentMappers;
    private final Registry<Key<?>, CommandCondition> conditions;
    private final Registry<Key<?>, Function<ArgumentModifier.Context, ArgumentModifier<?>>> modifiers;
    private final Registry<ExecutionStage, Queue<ExecutionListener>> listeners;
    private final CommandRegistrationHandler registrationHandler;

    CommandDispatcherImpl(DispatcherConfigurer configurer) {
        requireNonNull(configurer, "configurer");
        this.authorizer = requireNonNull(configurer.authorizer(), "authorizer cannot be null");
        this.argumentMappers = requireNonNull(configurer.argumentMappers(), "argumentMappers cannot be null");
        this.conditions = requireNonNull(configurer.conditions(), "conditions cannot be null");
        this.modifiers = requireNonNull(configurer.modifiers(), "modifiers cannot be null");
        this.listeners = requireNonNull(configurer.listeners(), "listener cannot be null");
        this.registrationHandler = requireNonNull(configurer.registrationHandler(), "registrationHandler cannot be null");
    }

    @Override
    public void register(Iterable<Command> commands) {
        requireNonNull(commands, "commands cannot be null");
        changeRegistrationState(commands, command -> {
            /*
             * Gather the runtime command information first. If this
             * fails due to a missing ArgumentMapper or CommandCondition,
             * we don't want to proceed any further.
             */
            CommandInfo commandInfo = buildCommand(command);
            // The registartion handler is invoked first
            this.registrationHandler.onRegister(command);
            // If the process wasn't interrupted, insert the command into the tree
            this.commandGraph.insert(command);
            // Cache argument chain
            this.knownCommands.store(command, commandInfo);
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
            this.knownCommands.remove(command);
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

    // Gather the runtime information of a command
    private CommandInfo buildCommand(Command command) {
        // Bind command arguments to argument mappers
        Map<Boolean, List<BoundArgument<?>>> argumentBindings = command.arguments()
                .stream()
                // Bake modifiers
                .peek(x -> x.modifierChain().bake($ -> this.modifiers.get($.key()).orElseThrow(
                        () -> new IllegalArgumentException("Could not find modifier factory for key '%s'".formatted($.key()))
                ).apply($.context()))) // Bake modifiers
                .map(this::bindArgument)
                .collect(partitioningBy(x -> x.argument().isFlag()));

        // Collect command conditions
        List<CommandCondition> conditions = command.spec()
                .conditions()
                .stream()
                .map(x -> this.conditions.get(Key.of(x)))
                .map(x -> x.orElseThrow(() -> new IllegalStateException("No condition was found for '%s'".formatted(x))))
                .toList();

        return new CommandInfo(command, argumentBindings.get(false), argumentBindings.get(true), conditions);
    }

    // This method exists to make working with generics less of a pain.
    @SuppressWarnings("unchecked")
    private <T> BoundArgument<T> bindArgument(CommandArgument<T> argument) {
        /*
         * Retrieve argument mapper. A correct mapper is assumed to be present
         * in the registry, hence we throw an error if that turns out not to be
         * the case. No mapper being present for this argument's mapper key would
         * indicate an improper/incomplete dispatcher configurer, which is a user
         * error.
         */
        Function<Key<T>, ArgumentMapper<T>> mapperAccess = key -> (ArgumentMapper<T>) this.argumentMappers.get(key)
                .orElseThrow(() -> new IllegalStateException("Could not find argument mapper matching '%s'. Requested by: '%s'".formatted(
                        argument.mapperKey(), argument
                )));

        return argument.bind(mapperAccess);
    }

    private CommandInfo requireInfo(Command command) {
        return this.knownCommands.get(command).orElseThrow(() -> new IllegalStateException(
                "No cached information was found for command '%s' The dispatcher is not configured properly.".formatted(command)
        ));
    }

    // Invoke command listeners
    private boolean invokeListeners(ExecutionStage stage, CommandContext context) {
        Optional<Queue<ExecutionListener>> listeners = this.listeners.get(stage);
        if (listeners.isEmpty()) return true;

        for (ExecutionListener listener : listeners.orElseThrow()) {
            if (!listener.handle(context)) return false;
        }

        return true;
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

        // Save the command instance so that we can retrieve it later if needed
        context.put(InternalContextKeys.COMMAND, command);
        if (!invokeListeners(ExecutionStage.PRE_PROCESS, context)) return;

        // Check permissions
        Optional<String> permission = command.spec().permission();
        boolean mayExecute = permission.map(x -> this.authorizer.authorize(x, context))
                .orElse(true);

        // Throw an exception if the user lacks sufficient permissions
        if (!mayExecute) throw new CommandAuthorizationException(permission.orElseThrow());

        // Retrieve command info
        CommandInfo commandInfo = requireInfo(command);
        // Check conditions
        for (CommandCondition condition : commandInfo.conditions()) {
            // If the result is false, we throw an exception.
            if (!condition.evaluate(context)) throw new UnfulfilledConditionException(condition);
        }

        // Parse command arguments and store the results in the current context
        ParseResult parseResult = parseArguments(context, input, commandInfo);
        // Rethrow captured exception if it exists
        parseResult.rethrowCaptured();

        if (!invokeListeners(ExecutionStage.PRE_EXECUTION, context)) return;
        // Finally, invoke the command
        command.run(context);

        invokeListeners(ExecutionStage.POST_EXECUTION, context);
    }

    private ParseResult parseArguments(CommandContext context, StringReader input, CommandInfo commandInfo) {
        ParseResult.Builder builder = ParseResult.parsing(commandInfo);
        Command command = commandInfo.command();
        try {
            String arg;
            while ((arg = input.peekSingle()) != null) {
                builder.consumed().consuming(arg);
                // Attempt to parse "arg" into a group of flags
                Optional<FlagGroup> flagGroup = FlagGroup.attemptParse(arg, commandInfo.flags());
                // Successfully parsed at least one flag
                if (flagGroup.isPresent()) {
                    // Read a single argument from the input so that the flag argument's mapper
                    // can parse the next in the list (ensuring that the argument mapper doesn't
                    // process the --flagname input part).
                    input.readSingle();
                    // Process each flag in this group
                    for (BoundArgument<?> flag : flagGroup.orElseThrow()) {
                        CommandArgument<?> argument = flag.argument();
                        builder.consuming(flag);

                        if (context.contains(argument.key())) {
                            // This means that the flag is already been set
                            throw new DuplicateFlagException(argument.name());
                        }

                        flag.consume(context, input);
                    }

                } else {
                    // Attempt to find the first unconsumed required command argument
                    BoundArgument<?> firstRequired = commandInfo.arguments()
                            .stream()
                            .filter(x -> !context.contains(x.argument().key()))
                            .findFirst()
                            .orElseThrow(() -> CommandSyntaxException.from(input, command, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS));

                    builder.consuming(firstRequired);
                    firstRequired.consume(context, input);
                }
            }

            /*
             * Verify that all non-flag arguments have been parsed. The reason we
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
            builder.capture(ex);
        }

        // return parseInfo;
        return builder.build();
    }

    @Override
    public List<String> complete(CommandContext context, String commandLine) {
        requireNonNull(context, "context cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        // Construct a new reader from user input
        StringReader input = new StringReaderImpl(commandLine, context);
        // Find the command instance to create completions for
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
        CommandInfo commandInfo = requireInfo(command);

        // Parse command
        ParseResult parseResult = parseArguments(context, input, commandInfo);

        /*
         * Fully consumed means there are no more required or flag arguments
         * left to parse. In this case, we return an empty list.
         */
        if (parseResult.fullyConsumed()) return List.of();

        return complete(context, parseResult, input);
    }

    private List<String> complete(
            CommandContext context,
            ParseResult parseResult,
            StringReader input
    ) {
        // First, attempt to read the remaining arguments.
        String remaining;
        try {
            remaining = input.readRemaining();
        } catch (CommandException ex) {
            // There's nothing to read, default to empty string
            remaining = "";
        }

        /*
         * Indicates whether to suggest the next command argument instead of
         * ParseInfo#argument. This will only evaluate to true, if remaining
         * is whitespace, but not an empty string.
         */
        boolean completeNext = remaining.isBlank() && !remaining.isEmpty();

        // Select the first unseen argument. Required arguments take precedence over flags.
        BoundArgument<?> firstUnseen = parseResult.remaining().isEmpty()
                ? parseResult.remainingFlags().get(0)
                : parseResult.remaining().get(0);

        /*
         * Select argument to create completions for. If suggestNext evaluates
         * to true (explained above), we don't care about the ParseInfo#argument,
         * otherwise prefer that over firstUnseen, if it's present.
         */
        BoundArgument<?> argument = completeNext
                ? firstUnseen
                : parseResult.lastUnsuccessfulArgument().orElse(firstUnseen);

        // The user input to create completions based on.
        String arg = completeNext ? remaining : parseResult.lastInput().orElse(remaining);

        if (arg.isEmpty()) return List.of();

        // Accumulate completions into this list
        List<String> base = new ArrayList<>();

        if (argument.argument().isFlag()) {
            if (argument.argument().asFlag().isPresenceFlag() && completeNext) {
                base.addAll(argument.mapper().complete(context, arg));
            } else {
                base.addAll(formatFlags(parseResult.remainingFlags()));
                // Flag shorthand is used
                if (arg.length() > 1 && Character.isAlphabetic(arg.charAt(1))) {
                    // Start completing a flag group
                    parseResult.remainingFlags().stream()
                            .map(x -> x.argument().asFlag().shorthand())
                            .filter(x -> arg.indexOf(x) == -1) // Only want to suggest flags that aren't already present in the group
                            .map(x -> "%s%s".formatted(arg, x))
                            .forEach(base::add);
                }

            }
        } else {
            base.addAll(argument.mapper().complete(context, arg));

            // If the current argument starts with '-', we list flags as well
            if (arg.startsWith(SHORT_FLAG_PREFIX)) {
                base.addAll(formatFlags(parseResult.remainingFlags()));
            }
        }

        return base.stream()
                .filter(x -> startsWithIgnoreCase(x, arg.trim()))
                .toList();
    }

    private List<String> formatFlags(Collection<BoundArgument<?>> flags) {
        return flags.stream()
                .map(BoundArgument::argument)
                .map(CommandArgument::asFlag)
                .map(x -> List.of(SHORT_FLAG_FORMAT.apply(x.shorthand()), LONG_FLAG_FORMAT.apply(x.name())))
                .flatMap(Collection::stream)
                .toList();
    }
}
