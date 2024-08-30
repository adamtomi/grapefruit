package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;

public interface CommandDispatcher {

    void register(Iterable<Command> commands);

    void dispatch(CommandContext context, String commandLine) throws CommandException;
}
