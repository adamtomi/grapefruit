package grapefruit.command.argument.condition;

import grapefruit.command.dispatcher.CommandContext;

import java.util.List;

import static java.util.Objects.requireNonNull;

final class OrCondition<S> implements CommandCondition<S> {
    private final List<CommandCondition<S>> conditions;

    OrCondition(final List<CommandCondition<S>> conditions) {
        this.conditions = requireNonNull(conditions, "conditions cannot be null");
    }

    @Override
    public void test(final CommandContext<S> context) throws UnfulfilledConditionException {
        UnfulfilledConditionException captured = null;
        boolean success = false;
        for (final CommandCondition<S> condition : this.conditions) {
            try {
                condition.test(context);
                success = true;
            } catch (final UnfulfilledConditionException ex) {
                captured = ex;
            }
        }

        if (!success && captured != null) throw captured;
    }
}
