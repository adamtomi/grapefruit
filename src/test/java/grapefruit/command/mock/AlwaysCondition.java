package grapefruit.command.mock;

import grapefruit.command.argument.condition.CommandCondition;
import grapefruit.command.argument.condition.UnfulfilledConditionException;
import grapefruit.command.dispatcher.CommandContext;

public class AlwaysCondition implements CommandCondition<Object> {
    private final boolean shouldPass;

    private AlwaysCondition(final boolean shouldPass) {
        this.shouldPass = shouldPass;
    }

    public static CommandCondition<Object> fail() {
        return new AlwaysCondition(false);
    }

    public static CommandCondition<Object> pass() {
        return new AlwaysCondition(true);
    }

    @Override
    public void test(final CommandContext<Object> context) throws UnfulfilledConditionException {
        if (!this.shouldPass) throw new UnfulfilledConditionException(this);
    }
}
