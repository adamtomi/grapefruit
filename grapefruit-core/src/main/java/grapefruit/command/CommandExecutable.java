package grapefruit.command;

import grapefruit.command.dispatcher.CommandContext;

@FunctionalInterface
public interface CommandExecutable {

    void execute(CommandContext context) throws CommandException;
}
