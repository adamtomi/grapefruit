package grapefruit.command.argument.condition;

import grapefruit.command.dispatcher.CommandContext;

import java.util.List;

final class AndCondition<S> implements CommandCondition<S> {
    private final List<CommandCondition<S>> conditions;

    AndCondition(final List<CommandCondition<S>> conditions) {
        this.conditions = conditions;
    }

    @Override
    public void test(final CommandContext<S> context) throws UnfulfilledConditionException {
        for (final CommandCondition<S> condition : this.conditions) condition.test(context);
    }
}
