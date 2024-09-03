package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl implements CommandDispatcher {
    private final CommandGraph commandGraph = new CommandGraph();
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
            // The registartion handler is invoked first
            this.registrationHandler.onRegister(command);
            // If the process wasn't interrupted, insert the command into the tree
            this.commandGraph.insert(command);
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

    @Override
    public void dispatch(CommandContext context, String commandLine) throws CommandException {
        requireNonNull(context, "context cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        // Construct a new reader from user input
        StringReader input = new StringReaderImpl(commandLine, context);
        // Find the command instance to execute
        Command command = this.commandGraph.search(input);

        // Check permissions
        Optional<String> permission = command.meta().permission();
        boolean mayExecute = permission.map(x -> this.authorizer.authorize(x, context))
                .orElse(true);

        // Throw an exception if the user lacks sufficient permissions
        if (!mayExecute) throw new CommandAuthorizationException(permission.orElseThrow());

        // Save the command instance so that we can retrieve it later if needed
        context.store(StandardContextKeys.COMMAND_INSTANCE, command);

        // Parse command arguments and store the results in the current context
        processArguments(context, input, command);

        // Finally, invoke the command
        command.run(context);
    }

    private void processArguments(CommandContext context, StringReader input, Command command) throws CommandException {
        // Collect flag arguments for later use
        @SuppressWarnings("rawtypes")
        List<FlagArgument> flagArguments = command.arguments().stream()
                .filter(CommandArgument::isFlag)
                .map(x -> (FlagArgument) x)
                .toList();

        String arg;
        while ((arg = input.peekSingle()) != null) {
            // Attempt to parse "arg" into a group of flags
            Optional<FlagGroup> flagGroup = FlagGroup.attemptParse(arg, flagArguments);
            // Successfully parsed at least one flag
            if (flagGroup.isPresent()) {
                // Read a single argument from the input so that the flag argument's mapper
                // can parse the next in the list (ensuring that the argument mapper doesn't
                // process the --flagname input part).
                input.readSingle();
                // Process each flag in this group
                for (FlagArgument<?> flag : flagGroup.orElseThrow()) {
                    if (context.getSafe(flag.key()).isPresent()) {
                        // This means that the flag is already been set
                        throw new DuplicateFlagException(flag.name());
                    }

                    if (flag.isPresenceFlag()) {
                        @SuppressWarnings("unchecked")
                        Key<Object> key = (Key<Object>) flag.key();
                        /*
                         * Set the value to true, since the flag was set
                         * and presence flags are true if set and false
                         * if not.
                         */
                        context.store(key, true);
                    } else {
                        // Map and store flag value
                        mapAndStoreArgument(context, input, flag);
                    }
                }

            } else {
                // Attempt to find the first unconsumed non-flag command argument
                CommandArgument<?> firstArgument = command.arguments().stream()
                        .filter(x -> !x.isFlag())
                        .filter(x -> context.getSafe(x.key()).isEmpty())
                        .findFirst()
                        .orElseThrow(() -> CommandSyntaxException.from(input, command, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS));

                // Map and store argument value
                mapAndStoreArgument(context, input, firstArgument);
            }
        }

        /*
         * Validate that all non-flag arguments have been parsed. The reason we
         * only check non-flags is that flags are optional, so omitting them
         * is perfectly valid.
         */
        for (CommandArgument<?> argument : command.arguments()) {
            if (!argument.isFlag() && context.getSafe(argument.key()).isEmpty()) {
                throw CommandSyntaxException.from(input, command, CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS);
            }
        }

        // Check if we have consumed all arguments
        if (input.hasNext()) throw CommandSyntaxException.from(input, command, CommandSyntaxException.Reason.TOO_MANY_ARGUMENTS);
    }

    // TODO rework argument-key relationship to support named argument mappers
    private void mapAndStoreArgument(CommandContext context, StringReader input, CommandArgument<?> argument) throws CommandException {
        Key<?> mapperKey = Key.of(argument.key().type());
        // Get the argument mapper
        ArgumentMapper<?> mapper = this.argumentMappers.get(mapperKey)
                .orElseThrow(() -> new IllegalStateException("No argument mapper was found for key '%s'".formatted(mapperKey)));

        Object mappedValue = mapper.tryMap(context, input);
        @SuppressWarnings("unchecked")
        Key<Object> key = (Key<Object>) argument.key();
        context.store(key, mappedValue);
    }

    @Override
    public List<String> suggestions(CommandContext context, String commandLine) {
        return List.of();
    }
}
