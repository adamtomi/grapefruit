package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandContainer;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.config.DispatcherConfigurer;

import java.util.List;

public interface CommandDispatcher {

    void register(Iterable<Command> commands);

    default void register(CommandContainer container) {
        register(container.commands());
    }

    void unregister(Iterable<Command> commands);

    default void unregister(CommandContainer container) {
        unregister(container.commands());
    }

    void dispatch(CommandContext context, String commandLine) throws CommandException;

    List<String> complete(CommandContext context, String commandLine);

    static CommandDispatcher using(DispatcherConfigurer... configurers) {
        return new CommandDispatcherImpl(DispatcherConfigurer.merge(configurers));
    }
}
