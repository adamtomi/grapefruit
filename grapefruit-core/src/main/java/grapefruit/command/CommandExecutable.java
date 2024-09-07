package grapefruit.command;

import grapefruit.command.dispatcher.CommandContext;

/**
 * An executable part of a command.
 * @see Command#execute(CommandContext)
 * @see grapefruit.command.argument.chain.BoundArgument#execute(CommandContext)
 */
@FunctionalInterface
public interface CommandExecutable {

    /**
     * Executes this part of the comand chain using the
     * provided context.
     *
     * @param context The current context
     * @throws CommandException If anything goes wrong during
     * the execution
     */
    void execute(CommandContext context) throws CommandException;
}
