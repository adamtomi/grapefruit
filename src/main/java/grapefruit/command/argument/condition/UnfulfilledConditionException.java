package grapefruit.command.argument.condition;

import grapefruit.command.CommandException;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class UnfulfilledConditionException extends CommandException {
    @Serial
    private static final long serialVersionUID = 4893249891589084580L;
    private final CommandCondition<?> condition;

    public UnfulfilledConditionException(final CommandCondition<?> condition) {
        this.condition = requireNonNull(condition, "condition cannot be null");
    }

    public CommandCondition<?> condition() {
        return this.condition;
    }
}
