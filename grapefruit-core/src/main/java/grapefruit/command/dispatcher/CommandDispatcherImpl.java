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
import grapefruit.command.dispatcher.condition.CommandCondition;
import grapefruit.command.dispatcher.condition.UnfulfilledConditionException;
import grapefruit.command.dispatcher.config.DispatcherConfigurer;
import grapefruit.command.dispatcher.input.StringReader;
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

import static grapefruit.command.dispatcher.syntax.CommandSyntax.LONG_FLAG_FORMAT;
import static grapefruit.command.dispatcher.syntax.CommandSyntax.SHORT_FLAG_FORMAT;
import static grapefruit.command.dispatcher.syntax.CommandSyntax.SHORT_FLAG_PREFIX;
import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl implements CommandDispatcher {
    private final CommandGraph commandGraph = new CommandGraph();
    private final Registry<Command, CommandInfo> knownCommands = Registry.create(Registry.DuplicateStrategy.reject());
    private final CommandAuthorizer authorizer;
    private final Registry<Key<?>, ArgumentMapper<?>> argumentMappers;
    private final Registry<Key<?>, CommandCondition> conditions;
    private final CommandRegistrationHandler registrationHandler;

    CommandDispatcherImpl(DispatcherConfigurer configurer) {
        requireNonNull(configurer, "configurer");
        this.authorizer = requireNonNull(configurer.authorizer(), "authorizer cannot be null");
        this.argumentMappers = requireNonNull(configurer.argumentMappers(), "argumentMappers cannot be null");
        this.conditions = requireNonNull(configurer.conditions(), "conditions cannot be null");
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

    private CommandInfo buildCommand(Command command) {
        ArgumentChain argumentChain = createChain(command);
        List<CommandCondition> conditions = command.spec()
                .conditions()
                .stream()
                .map(x -> this.conditions.get(Key.of(x)))
                .map(x -> x.orElseThrow(() -> new IllegalStateException("No condition was found for '%s'".formatted(x))))
                .toList();

        return new CommandInfo(command, argumentChain, conditions);
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

            if (argument.isFlag()) {
                flags.add((BoundArgument.Flag<?>) argument.bind(
                        ((FlagArgument<?>) argument).isPresenceFlag()
                                ? (ArgumentMapper) ArgumentMapper.constant(true)
                                : needMapper((Key) mapperKey, argument)
                ));
            } else {
                positional.add((BoundArgument.Positional<?>) argument.bind(needMapper((Key) mapperKey, argument)));
            }
        }

        return ArgumentChain.create(positional, flags);
    }

    @SuppressWarnings("unchecked")
    private <T> ArgumentMapper<T> needMapper(Key<T> key, CommandArgument<T> argument) {
        return (ArgumentMapper<T>) this.argumentMappers.get(key)
                .orElseThrow(() -> new IllegalStateException("Could not find argument mapper matching '%s'. Requested by: '%s'".formatted(
                        key,
                        argument
                )));
    }

    private CommandInfo requireInfo(Command command) {
        return this.knownCommands.get(command).orElseThrow(() -> new IllegalStateException(
                "No cached information was found for command '%s' The dispatcher is not configured properly.".formatted(command)
        ));
    }

    @Override
    public void dispatch(CommandContext context, String commandLine) throws CommandException {
        requireNonNull(context, "context cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        // Construct a new reader from user input
        StringReader input = context.createReader(commandLine);
        // Find the command instance to execute
        CommandGraph.SearchResult search = this.commandGraph.search(input);
        if (search instanceof CommandGraph.SearchResult.Failure failure) throw failure.cause();

        // The search was successful, we can extract the command instance
        Command command = ((CommandGraph.SearchResult.Success) search).command();

        // Check permissions
        Optional<String> permission = command.spec().permission();
        boolean mayExecute = permission.map(x -> this.authorizer.authorize(x, context))
                .orElse(true);

        // Throw an exception if the user lacks sufficient permissions
        if (!mayExecute) throw new CommandAuthorizationException(permission.orElseThrow());

        // Save the command instance so that we can retrieve it later if needed
        context.put(InternalContextKeys.COMMAND, command);

        // Retrieve command info
        CommandInfo commandInfo = requireInfo(command);
        // Check conditions
        for (CommandCondition condition : commandInfo.conditions()) {
            // If the result is false, we throw an exception.
            if (!condition.evaluate(context)) throw new UnfulfilledConditionException(condition);
        }

        // Parse command arguments and store the results in the current context
        ParseInfo parseInfo = parseArguments(context, input, command, commandInfo.argumentChain());
        // Rethrow captured exception if it exists
        if (parseInfo.capturedException().isPresent()) {
            throw parseInfo.capturedException().orElseThrow();
        }

        // Finally, invoke the command
        command.execute(context);
    }

    private ParseInfo parseArguments(CommandContext context, StringReader input, Command command, ArgumentChain argumentChain) {
        ParseInfo parseInfo = context.createParseInfo();
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

                        flag.execute(context);
                    }

                } else {
                    // Attempt to find the first unconsumed positional command argument
                    BoundArgument.Positional<?> firstPositional = argumentChain.positional()
                            .stream()
                            .filter(x -> !context.contains(x.argument().key()))
                            .findFirst()
                            .orElseThrow(() -> CommandSyntaxException.from(input, command, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS));

                    parseInfo.argument(firstPositional);
                    firstPositional.execute(context);
                }
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

    @Override
    public List<String> suggestions(CommandContext context, String commandLine) {
        requireNonNull(context, "context cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        // Construct a new reader from user input
        StringReader input = context.createReader(commandLine);
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
        ArgumentChain argumentChain = requireInfo(command).argumentChain();

        // Parse command
        ParseInfo parseInfo = parseArguments(context, input, command, argumentChain);
        /*if (parseInfo.capturedException().isEmpty()) {
            // If we successfully process every single argument, the
            // command is complete, we don't need to suggest anything else.
            return List.of();
        } else {
            // Return a list of suggestions based on the parse result.
            return suggestions(context, parseInfo, input, argumentChain);
        }*/
        return suggestions(context, parseInfo, input, argumentChain);
    }

    // TODO Proper flag group suggestions
    private List<String> _suggestions(
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
            // Attempt to read the remaining arguments
            remaining = input.readRemaining();
            System.out.println("Remaining is: '%s'".formatted(remaining));
        } catch (CommandException ex) {
            System.out.println("no remaining string");
            remaining = "";
        }

        String arg0 = parseInfo.input().orElse(remaining);
        System.out.println("Arg is: '%s'".formatted(arg0));

        if (arg0.isEmpty()) {
            System.out.println("Returning empty list, can't do anything else.");
            // The space hasn't been pressed yet. So the input is something like:
            // 'some route arg0 arg1'. Don't suggest anything.
            return List.of();
        }

        String arg = remaining.isBlank() ? remaining : arg0;
        BoundArgument<?, ?> firstUnseen = unseenPositional.isEmpty()
                ? unseenFlags.get(0)
                : unseenPositional.get(0);

        BoundArgument<?, ?> argToParse = parseInfo.argument()
                .filter(x -> arg.isBlank() && !arg.isEmpty() ? x.equals(firstUnseen) : true)
                .orElse(firstUnseen);

        System.out.println("unseen: " + firstUnseen.argument());
        System.out.println("final: " + argToParse.argument());

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

    private List<String> suggestions(
            CommandContext context,
            ParseInfo parseInfo,
            StringReader input,
            ArgumentChain argumentChain
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
        boolean suggestNext = remaining.isBlank() && !remaining.isEmpty();

        // Collect unseen required and flag arguments
        List<BoundArgument.Positional<?>> unseenRequireds = argumentChain.positional()
                .stream()
                .filter(x -> !context.contains(x.argument().key()))
                .toList();
        List<BoundArgument.Flag<?>> unseenFlags = argumentChain.flag()
                .stream()
                .filter(x -> !context.contains(x.argument().key()))
                .toList();
        
        // Select first unseen argument. Required arguments take precedence over flags.
        BoundArgument<?, ?> firstUnseen = unseenRequireds.isEmpty()
                ? unseenFlags.get(0)
                : unseenRequireds.get(0);

        /*
         * Select argument to create suggestions for. If suggestNext evaluates
         * to true (explained above), we don't care about the ParseInfo#argument,
         * otherwise prefer that over firstUnseen, if it's present.
         */
        BoundArgument<?, ?> argument = suggestNext
                ? firstUnseen
                : parseInfo.argument().orElse(firstUnseen);

        // The user input to create suggestions based on.
        String arg = suggestNext ? remaining : parseInfo.input().orElse(remaining);

        if (argument instanceof BoundArgument.Flag<?>) {
            System.out.println("is flag");
            if (parseInfo.suggestFlagValue() && suggestNext) {
                System.out.println("suggesting flag values (if possible)");
                return argument.mapper().listSuggestions(context, arg);
            } else {
                System.out.println("suggest flag names");
                // Create mutable copy
                List<String> base =  new ArrayList<>(formatFlags(unseenFlags));
                // Flag shorthand is used
                if (Character.isAlphabetic(arg.charAt(1))) {
                    System.out.println("looks like a shorthand was used, completing flag group...");
                    // Start completing a flag group
                    unseenFlags.stream()
                            .map(x -> x.argument().shorthand())
                            .filter(x -> arg.indexOf(x) == -1) // Only want to suggest flags that aren't already present in the group
                            .map(x -> "%s%s".formatted(arg, x))
                            .forEach(base::add);
                }

                return base;
            }
        } else {
            System.out.println("not a flag argument");
            // Make a mutable copy of the list
            List<String> base = new ArrayList<>(argument.mapper().listSuggestions(context, arg));

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
                .map(x -> List.of(SHORT_FLAG_FORMAT.apply(x.shorthand()), LONG_FLAG_FORMAT.apply(x.name())))
                .flatMap(Collection::stream)
                .toList();
    }
}
