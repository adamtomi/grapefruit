package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;

import java.util.List;

public interface CommandDispatcher {

    void register(Iterable<Command> commands);

    void unregister(Iterable<Command> commands);

    void dispatch(CommandContext context, String commandLine) throws CommandException;

    List<String> suggestions(CommandContext context, String commandLine);
}
