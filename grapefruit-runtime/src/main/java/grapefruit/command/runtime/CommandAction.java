package grapefruit.command.runtime;

import grapefruit.command.runtime.dispatcher.CommandContext;

/**
 * The command action is the executable part of a command.
 * It is invoked after user input has successfully been
 * parsed.
 */
@FunctionalInterface
public interface CommandAction {

    /**
     * Executes the command handler method.
     *
     * @param context The current context
     * @throws CommandException If anything goes wrong during
     * the execution
     */
    void invoke(CommandContext context) throws CommandException;
}
