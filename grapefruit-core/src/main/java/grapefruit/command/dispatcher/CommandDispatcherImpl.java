package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.auth.CommandAuthorizationException;
import grapefruit.command.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.dispatcher.input.StringReader;
import grapefruit.command.dispatcher.input.StringReaderImpl;
import grapefruit.command.dispatcher.syntax.DuplicateFlagException;
import grapefruit.command.dispatcher.tree.CommandGraph;
import grapefruit.command.util.FlagGroup;
import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl implements CommandDispatcher {
    private final CommandGraph commandGraph = new CommandGraph();
    private final CommandAuthorizer authorizer;
    private final Registry<Key<?>, ArgumentMapper<?>> argumentMappers;

    CommandDispatcherImpl(CommandAuthorizer authorizer, Registry<Key<?>, ArgumentMapper<?>> argumentMappers) {
        this.authorizer = requireNonNull(authorizer, "authorizer cannot be null");
        this.argumentMappers = requireNonNull(argumentMappers, "argumentMappers cannot be null");
    }

    @Override
    public void register(Iterable<Command> commands) {
        requireNonNull(commands, "commands cannot be null");
        commands.forEach(this.commandGraph::insert);
    }

    @Override
    public void unregister(Iterable<Command> commands) {
        requireNonNull(commands, "commands cannot be null");
        commands.forEach(this.commandGraph::delete);
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
        processArguments(context, input, command.arguments());

        // Finally, invoke the command
        command.run(context);
    }

    private void processArguments(CommandContext context, StringReader input, List<CommandArgument<?>> arguments) throws CommandException {
        // Collect flag arguments for later use
        @SuppressWarnings("rawtypes")
        List<FlagArgument> flagArguments = arguments.stream()
                .filter(CommandArgument::isFlag)
                .map(x -> (FlagArgument) x)
                .toList();

        String arg;
        while ((arg = input.peekSingle()) != null) {
            // Construct a new matcher to detect flags
            Matcher flagMatcher = FlagGroup.VALID_PATTERN.matcher(arg);
            // If flagMatcher.matches is true, that means we're dealing with at least one flag.
            if (flagMatcher.matches()) {
                FlagGroup flagGroup = FlagGroup.parse(flagMatcher, flagArguments);
                // Read a single argument from the input so that the flag argument's mapper
                // can parse the next in the list (ensuring that the argument mapper doesn't
                // process the --flagname input part).
                input.readSingle();
                // Process each flag in this group
                for (FlagArgument<?> flag : flagGroup) {
                    if (context.getSafe(flag.key()).isEmpty()) {
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
                CommandArgument<?> firstArgument = arguments.stream()
                        .filter(x -> !x.isFlag())
                        .filter(x -> context.getSafe(x.key()).isEmpty())
                        .findFirst()
                        // TODO figure out which exception to throw lol
                        .orElseThrow(CommandException::new);

                // Map and store argument value
                mapAndStoreArgument(context, input, firstArgument);
            }
        }
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
