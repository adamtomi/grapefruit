package grapefruit.command;

import grapefruit.command.dispatcher.CommandContext;

@FunctionalInterface
public interface CommandAction {

    void run(CommandContext context) throws CommandException;
}
