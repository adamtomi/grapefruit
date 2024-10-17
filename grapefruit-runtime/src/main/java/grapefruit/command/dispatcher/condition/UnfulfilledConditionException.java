package grapefruit.command.dispatcher.condition;

import grapefruit.command.CommandException;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

/**
 * Signals that a certain {@link CommandCondition} evaluated to false.
 */
public class UnfulfilledConditionException extends CommandException {
    @Serial
    private static final long serialVersionUID = 1015644609056533586L;
    private final CommandCondition condition;

    public UnfulfilledConditionException(CommandCondition condition) {
        this.condition = requireNonNull(condition, "condition cannot be null");
    }

    /**
     * @return The condition that caused this exception to be thrown.
     */
    public CommandCondition condition() {
        return this.condition;
    }
}
