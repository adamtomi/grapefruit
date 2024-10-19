package grapefruit.command.runtime.dispatcher.condition;

import grapefruit.command.runtime.dispatcher.CommandContext;

/**
 * A command condition determines whether the execution
 * of a particular command may proceed to the next (parsing)
 * stage.
 */
@FunctionalInterface
public interface CommandCondition {

    /**
     * Checks whether the execution of the current
     * command should progress to the next phase.
     *
     * @param context The current command context
     * @return Whether execution should continue
     */
    boolean evaluate(CommandContext context);
}
