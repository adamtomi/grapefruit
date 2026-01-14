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

    private void doTest() throws UnfulfilledConditionException {
        if (!this.shouldPass) throw new UnfulfilledConditionException(this);
    }

    @Override
    public void testEarly(final CommandContext<Object> context) throws UnfulfilledConditionException {
        doTest();
    }

    @Override
    public void testLate(final CommandContext<Object> context) throws UnfulfilledConditionException {
        doTest();
    }
}
