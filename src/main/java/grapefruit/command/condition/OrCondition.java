package grapefruit.command.condition;

import grapefruit.command.dispatcher.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class OrCondition<S> extends AbstractMultiCondition<S> {
    private static final String ID = "OR";

    OrCondition(final @NotNull List<CommandCondition<S>> conditions) {
        super(ID, conditions);
    }

    @Override
    public void test(final @NotNull CommandContext<S> context) throws ConditionFailedException {
        @Nullable ConditionFailedException lastException = null;
        for (final CommandCondition<S> condition : this.conditions) {
            try {
                condition.test(context);
                return; // One condition passing is enough
            } catch (final ConditionFailedException ex) {
                lastException = ex;
            }
        }

        if (lastException != null) {
            throw lastException;
        }
    }
}
