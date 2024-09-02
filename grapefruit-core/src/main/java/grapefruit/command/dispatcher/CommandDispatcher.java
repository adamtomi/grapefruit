package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.dispatcher.config.DispatcherConfigurer;
import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandDispatcher {

    void register(Iterable<Command> commands);

    void unregister(Iterable<Command> commands);

    void dispatch(CommandContext context, String commandLine) throws CommandException;

    List<String> suggestions(CommandContext context, String commandLine);

    static CommandDispatcher using(DispatcherConfigurer... configurers) {
        return new CommandDispatcherImpl(DispatcherConfigurer.merge(configurers));
    }
}
