package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.CommandException;
import grapefruit.command.runtime.dispatcher.config.DispatcherConfigurer;
import grapefruit.command.runtime.generated.CommandContainer;
import grapefruit.command.runtime.generated.CommandMirror;

import java.util.List;

/**
 * A command dispatcher is responsible for a variety of command-related
 * tasks, thus can be considered the very core of this library.
 */
public interface CommandDispatcher {

    /**
     * Registers the supplied commands. This process involves constructing
     * a {@link CommandDefinition} instance of each command, invoking the
     * active {@link CommandRegistrationHandler} instance and finally
     * inserting each command into the command tree.
     *
     * @param commands The commands to register
     * @see grapefruit.command.runtime.dispatcher.tree.CommandGraph#insert(List, CommandDefinition)
     */
    void register(Iterable<CommandMirror> commands);

    /**
     * Registers commands found in the supplied container.
     *
     * @param container The container
     * @see this#register(Iterable)
     * @see CommandContainer#commands()
     */
    default void register(CommandContainer container) {
        register(container.commands());
    }

    /**
     * Unregisters the supplied commands. This process involves invoking
     * the active {@link CommandRegistrationHandler} instance and removing
     * each command from the command tree.
     *
     * @param commands The commands to unregister
     * @see grapefruit.command.runtime.dispatcher.tree.CommandGraph#delete(List)
     */
    void unregister(Iterable<CommandMirror> commands);

    /**
     * Unregisters commands found in the supplied container.
     *
     * @param container The container
     * @see this#register(Iterable)
     * @see CommandContainer#commands()
     */
    default void unregister(CommandContainer container) {
        unregister(container.commands());
    }

    void dispatch(CommandContext context, String commandLine) throws CommandException;

    List<String> complete(CommandContext context, String commandLine);

    static CommandDispatcher using(DispatcherConfigurer... configurers) {
        return new CommandDispatcherImpl(DispatcherConfigurer.merge(configurers));
    }
}
