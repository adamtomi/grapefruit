package grapefruit.command;

import grapefruit.command.dispatcher.CommandContext;

/**
 * An executable part of a command.
 * @see Command#run(CommandContext)
 */
@FunctionalInterface
public interface CommandAction {

    /**
     * Executes this part of the comand chain using the
     * provided context.
     *
     * @param context The current context
     * @throws CommandException If anything goes wrong during
     * the execution
     */
    void run(CommandContext context) throws CommandException;
}
