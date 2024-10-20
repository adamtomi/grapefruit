package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.CommandException;
import grapefruit.command.runtime.argument.CommandArgument;
import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.argument.modifier.ArgumentModifier;
import grapefruit.command.runtime.argument.modifier.ModifierBlueprint;
import grapefruit.command.runtime.dispatcher.auth.CommandAuthorizationException;
import grapefruit.command.runtime.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.runtime.dispatcher.condition.CommandCondition;
import grapefruit.command.runtime.dispatcher.condition.UnfulfilledConditionException;
import grapefruit.command.runtime.dispatcher.config.DispatcherConfigurer;
import grapefruit.command.runtime.dispatcher.input.StringReader;
import grapefruit.command.runtime.dispatcher.input.StringReaderImpl;
import grapefruit.command.runtime.dispatcher.syntax.CommandSyntaxException;
import grapefruit.command.runtime.dispatcher.syntax.DuplicateFlagException;
import grapefruit.command.runtime.dispatcher.tree.CommandGraph;
import grapefruit.command.runtime.generated.ArgumentMirror;
import grapefruit.command.runtime.generated.CommandMirror;
import grapefruit.command.runtime.util.FlagGroup;
import grapefruit.command.runtime.util.Registry;
import grapefruit.command.runtime.util.key.Key;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static grapefruit.command.runtime.argument.CommandArgument.Flag.PRESENCE_FLAG_TYPE;
import static grapefruit.command.runtime.dispatcher.InternalContextKeys.COMMAND;
import static grapefruit.command.runtime.dispatcher.syntax.CommandSyntax.LONG_FLAG_FORMAT;
import static grapefruit.command.runtime.dispatcher.syntax.CommandSyntax.SHORT_FLAG_FORMAT;
import static grapefruit.command.runtime.dispatcher.syntax.CommandSyntax.SHORT_FLAG_PREFIX;
import static grapefruit.command.runtime.util.StringUtil.startsWithIgnoreCase;
import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl implements CommandDispatcher {
    private final CommandGraph commandGraph = new CommandGraph();
    private final CommandAuthorizer authorizer;
    private final Registry<Key<?>, ArgumentMapper<?>> argumentMappers;
    private final Registry<Key<?>, CommandCondition> conditions;
    private final Registry<Key<?>, ArgumentModifier.Factory<?>> modifiers;
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
    public void register(Iterable<CommandMirror> commands) {
        requireNonNull(commands, "commands cannot be null");
        for (CommandMirror command : commands) {
            /*
             * Gather runtime command information first. If this fails
             * due to a missing ArgumentMapper or CommandCondition,
             * we don't want to proceed any further.
             */
            CommandDefinition commandDef = buildCommand(command);
            // The registartion handler is invoked first
            boolean shouldProceed = this.registrationHandler.register(command);
            // Got false, skipping registration
            if (!shouldProceed) return;
            // If the process wasn't interrupted, insert the command into the tree
            this.commandGraph.insert(command.route(), commandDef);
        }
    }

    @Override
    public void unregister(Iterable<CommandMirror> commands) {
        requireNonNull(commands, "commands cannot be null");
        for (CommandMirror command : commands) {
            // The registration handler is invoked first
            boolean shouldProceed = this.registrationHandler.unregister(command);
            // Got false, skipping registration
            if (!shouldProceed) return;
            // If the process wasn't interrupted, delete the command from the tree
            this.commandGraph.delete(command.route());
        }
    }

    // Gather runtime command information
    @SuppressWarnings("unchecked")
    private CommandDefinition buildCommand(CommandMirror command) {
        List<CommandArgument<?>> arguments = new ArrayList<>();
        List<CommandArgument.Flag<?>> flags = new ArrayList<>();

        // Gather runtime argument information
        for (ArgumentMirror<?> mirror : command.arguments()) {
            if (mirror instanceof ArgumentMirror.Flag<?> flag) {
                CommandArgument.Flag<?> builtFlag = flag.key().type().equals(PRESENCE_FLAG_TYPE)
                        ? buildPresenceFlag((ArgumentMirror.Flag<Boolean>) flag)
                        : buildValueFlag(flag);

                flags.add(builtFlag);
            } else {
                arguments.add(buildArgument(mirror));
            }
        }

        // Gather runtime condition information
        List<CommandCondition> conditions = command.conditions().stream()
                .map(x -> this.conditions.get(Key.of(x)).orElseThrow(() -> new IllegalStateException(
                        "Could not find command condition matching %s".formatted(x))
                ))
                .toList();

        return CommandDefinition.builder()
                .permission(command.permission().orElse(null))
                .arguments(arguments)
                .flags(flags)
                .conditions(conditions)
                .action(command.action())
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> ArgumentMapper<T> requireMapper(ArgumentMirror<T> mirror) {
        Key<T> key = mirror.mapperKey();
        return (ArgumentMapper<T>) this.argumentMappers.get(key).orElseThrow(() -> new IllegalStateException(
                "Could not find argument mapper for key '%s', requested by argument '%s'".formatted(key, mirror)
        ));
    }

    @SuppressWarnings("unchecked")
    private <T> List<ArgumentModifier<T>> collectModifiers(ArgumentMirror<T> mirror) {
        List<ArgumentModifier<T>> modifiers = new ArrayList<>();
        for (ModifierBlueprint modifier : mirror.modifiers()) {
            ArgumentModifier.Factory<T> factory = (ArgumentModifier.Factory<T>) this.modifiers.get(modifier.key())
                    .orElseThrow(() -> new IllegalStateException("Could not find argument modifier for key '%s', requested by argument '%s'".formatted(
                            modifier.key(), mirror
                    )));

            modifiers.add(factory.createFromContext(modifier.context()));
        }

        // Create immutable copy
        return List.copyOf(modifiers);
    }

    private <T> CommandArgument<T> buildArgument(ArgumentMirror<T> mirror) {
        return CommandArgument.required(mirror.name(), mirror.key())
                .withMapper(requireMapper(mirror))
                .withModifiers(collectModifiers(mirror))
                .build();

    }

    private CommandArgument.Flag<Boolean> buildPresenceFlag(ArgumentMirror.Flag<Boolean> mirror) {
        return CommandArgument.presenceFlag(mirror.name(), mirror.key())
                .shorthand(mirror.shorthand())
                .build();
    }

    private <T> CommandArgument.Flag<T> buildValueFlag(ArgumentMirror.Flag<T> mirror) {
        return CommandArgument.valueFlag(mirror.name(), mirror.key())
                .shorthand(mirror.shorthand())
                .withMapper(requireMapper(mirror))
                .withModifiers(collectModifiers(mirror))
                .build();
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
        CommandDefinition command = ((CommandGraph.SearchResult.Success) search).command();

        // Save the command instance so that we can retrieve it later if needed
        // context.put(InternalContextKeys.COMMAND, command);
        if (!invokeListeners(ExecutionStage.PRE_PROCESS, context)) return;

        // Check permissions
        Optional<String> permission = command.permission();
        boolean mayExecute = permission.map(x -> this.authorizer.authorize(x, context))
                .orElse(true);

        // Throw an exception if the user lacks sufficient permissions
        if (!mayExecute) throw new CommandAuthorizationException(permission.orElseThrow());

        // Check conditions
        for (CommandCondition condition : command.conditions()) {
            // If the result is false, we throw an exception.
            if (!condition.evaluate(context)) throw new UnfulfilledConditionException(condition);
        }

        // Parse command arguments and store the results in the current context
        ParseResult parseResult = parseArguments(context, command, input);
        // Rethrow captured exception if it exists
        parseResult.rethrowCaptured();

        if (!invokeListeners(ExecutionStage.PRE_EXECUTION, context)) return;
        // Finally, invoke the command
        command.invoke(context);

        invokeListeners(ExecutionStage.POST_EXECUTION, context);
    }

    private ParseResult parseArguments(CommandContext context, CommandDefinition command, StringReader input) {
        ParseResult.Builder builder = ParseResult.parsing(command);
        try {
            String arg;
            while ((arg = input.peekSingle()) != null) {
                builder /* .consumed() */.consuming(arg);
                // Attempt to parse "arg" into a group of flags
                Optional<FlagGroup> flagGroup = FlagGroup.attemptParse(arg, command.flags());
                // Successfully parsed at least one flag
                if (flagGroup.isPresent()) {
                    // Read a single argument from the input so that the flag argument's mapper
                    // can parse the next in the list (ensuring that the argument mapper doesn't
                    // process the --flagname input part).
                    input.readSingle();
                    // Process each flag in this group
                    for (CommandArgument.Flag<?> flag : flagGroup.orElseThrow()) {
                        builder.consuming(input.peekSingle()).consuming(flag);

                        if (context.contains(flag.key())) {
                            // This means that the flag is already been set
                            throw new DuplicateFlagException(flag.name());
                        }

                        consumeArgument(flag, context, input);
                        builder.consumed();
                    }

                } else {
                    // Attempt to find the first unseen required command argument
                    CommandArgument<?> firstUnseen = command.arguments()
                            .stream()
                            .filter(x -> !context.contains(x.key()))
                            .findFirst()
                            .orElseThrow(() -> CommandSyntaxException.from(input, context.nullable(COMMAND), command, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS));

                    builder.consuming(firstUnseen);
                    consumeArgument(firstUnseen, context, input);
                    if (arg.endsWith(" ")) builder.consumed();
                }
            }

            /*
             * Verify that all non-flag arguments have been parsed. The reason we
             * only check non-flags is that flags are optional, so omitting them
             * is perfectly valid.
             */
            for (CommandArgument<?> argument : command.arguments()) {
                if (!argument.isFlag() && !context.contains(argument.key())) {
                    throw CommandSyntaxException.from(input, context.nullable(COMMAND), command, CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS);
                }
            }

            // Check if we have consumed all arguments
            if (input.hasNext()) throw CommandSyntaxException.from(input, context.nullable(COMMAND), command, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS);
        } catch (CommandException ex) {
            builder.capture(ex);
        }

        return builder.build();
    }

    private <T> void consumeArgument(CommandArgument<T> argument, CommandContext context, StringReader input) throws CommandException {
        // First, run the argument mapper
        T mappedValue = argument.mapper().tryMap(context, input);
        // Then, we apply all argument modifiers
        for (ArgumentModifier<T> modifier : argument.modifiers()) mappedValue = modifier.apply(mappedValue);
        // Finally, store the resulting value in the command context
        context.put(argument.key(), mappedValue);
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
        if (searchResult instanceof CommandGraph.SearchResult.Failure failure) {
            if (input.hasNext()) {
                return List.of();
            }

            String prefix = failure.cause() instanceof CommandGraph.NoSuchCommandException noSuchCommandException
                    ? noSuchCommandException.name()
                    : "";
            return failure.validOptions(true)
                    .stream()
                    .filter(x -> startsWithIgnoreCase(x, prefix))
                    .toList();
        }

        // Command can safely be extracted
        CommandDefinition command = ((CommandGraph.SearchResult.Success) searchResult).command();

        // Parse command
        ParseResult parseResult = parseArguments(context, command, input);
        // If either DuplicateFlagException or UnrecognizedFlagException was thrown,
        // return an empty list.
        if (
                (parseResult.wasCaptured(DuplicateFlagException.class) || parseResult.wasCaptured(UnrecognizedFlagException.class))
                && input.hasNext()
        ) {
            return List.of();
        }

        // TODO check if input ends with "' '"
        /*
         * Fully consumed means there are no more required or flag arguments
         * left to parse. In this case, we return an empty list.
         */
        if (parseResult.fullyConsumed()) return List.of();


        return completeArguments(context, parseResult, input);
    }

    // TODO store parse data per argument. For instance for flags we need to know if we're completing the name or the value.
    private List<String> completeArguments(
            CommandContext context,
            ParseResult parseResult,
            StringReader input
    ) {
        /*
         * Indicates whether to suggest the next command argument instead of
         * ParseInfo#argument. This will only evaluate to true, if remaining
         * is whitespace, but not an empty string.
         */
        boolean completeNext = input.unwrap().endsWith(" ");

        // Attempt to read the remaining arguments.
        String remaining;
        try {
            remaining = input.readRemaining();
        } catch (CommandException ex) {
            // There's nothing to read, default to empty string
            remaining = "";
        }

        // Select the first unseen argument. Required arguments take precedence over flags.
        CommandArgument<?> firstUnseen = parseResult.remaining().isEmpty()
                ? parseResult.remainingFlags().getFirst()
                : parseResult.remaining().getFirst();

        /*
         * Select argument to create completions for. If suggestNext evaluates
         * to true (explained above), we don't care about the ParseInfo#argument,
         * otherwise prefer that over firstUnseen, if it's present.
         */
        CommandArgument<?> argument = parseResult.lastUnsuccessfulArgument().orElse(firstUnseen);

        // The user input to create completions based on.
        String arg = completeNext ? remaining : parseResult.lastInput().orElse(remaining);

        // Accumulate completions into this list
        List<String> base = new ArrayList<>();

        if (argument.isFlag()) {
            if (argument.asFlag().isPresence() && completeNext) {
                base.addAll(formatFlag(firstUnseen.asFlag()));
            } else {
                // TODO only include these suggestions, if a) completeNext is true or b) the flag name (or group) has been completed already
                base.addAll(argument.mapper().complete(context, arg));
                // Flag shorthand is used
                if (arg.length() > 1 && Character.isAlphabetic(arg.charAt(1))) {
                    // Start completing a flag group
                    parseResult.remainingFlags().stream()
                            .map(x -> x.asFlag().shorthand())
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

    private List<String> formatFlags(Collection<CommandArgument.Flag<?>> flags) {
        return flags.stream()
                .map(CommandArgument::asFlag)
                .map(this::formatFlag)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<String> formatFlag(CommandArgument.Flag<?> flag) {
        return List.of(SHORT_FLAG_FORMAT.apply(flag.shorthand()), LONG_FLAG_FORMAT.apply(flag.name()));
    }
}
