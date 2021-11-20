package grapefruit.command.condition;

import grapefruit.command.dispatcher.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class AndCondition<S> extends AbstractMultiCondition<S> {
    private static final String ID = "AND";

    AndCondition(final @NotNull List<CommandCondition<S>> conditions) {
        super(ID, conditions);
    }

    @Override
    public void test(final @NotNull CommandContext<S> context) throws ConditionFailedException {
        for (final CommandCondition<S> condition : this.conditions) {
            condition.test(context);
        }
    }
}
