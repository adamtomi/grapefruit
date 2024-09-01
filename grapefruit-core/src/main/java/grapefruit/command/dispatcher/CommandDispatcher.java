package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;

import java.util.List;

public interface CommandDispatcher {

    void register(Iterable<Command> commands);

    void unregister(Iterable<Command> commands);

    void dispatch(CommandContext context, String commandLine) throws CommandException;

    List<String> suggestions(CommandContext context, String commandLine);

    static CommandDispatcher create(CommandAuthorizer authorizer, Registry<Key<?>, ArgumentMapper<?>> argumentMappers) {
        return new CommandDispatcherImpl(authorizer, argumentMappers, null);
    }
}
