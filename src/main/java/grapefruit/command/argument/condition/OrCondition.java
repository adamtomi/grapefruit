package grapefruit.command.argument.condition;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.util.function.CheckedConsumer;

import java.util.List;

import static java.util.Objects.requireNonNull;

final class OrCondition<S> implements CommandCondition<S> {
    private final List<CommandCondition<S>> conditions;

    OrCondition(final List<CommandCondition<S>> conditions) {
        this.conditions = requireNonNull(conditions, "conditions cannot be null");
    }

    @Override
    public void testEarly(final CommandContext<S> context) throws UnfulfilledConditionException {
        doTest(x -> x.testEarly(context));
    }

    @Override
    public void testLate(final CommandContext<S> context) throws UnfulfilledConditionException {
        doTest(x -> x.testLate(context));
    }

    private void doTest(final CheckedConsumer<CommandCondition<S>, UnfulfilledConditionException> action) throws UnfulfilledConditionException {
        UnfulfilledConditionException captured = null;
        boolean success = false;
        for (final CommandCondition<S> condition : this.conditions) {
            try {
                action.accept(condition);
                success = true;
            } catch (final UnfulfilledConditionException ex) {
                captured = ex;
            }
        }

        if (!success && captured != null) throw captured;
    }
}
