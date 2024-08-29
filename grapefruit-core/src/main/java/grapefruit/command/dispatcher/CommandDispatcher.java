package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;

public interface CommandDispatcher {

    void register(Command... commands);

    // TODO proper command source type
    void dispatch(Object source, String commandLine) throws CommandException;
}
