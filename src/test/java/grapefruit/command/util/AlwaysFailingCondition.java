package grapefruit.command.util;

import grapefruit.command.argument.condition.CommandCondition;
import grapefruit.command.argument.condition.UnfulfilledConditionException;
import grapefruit.command.dispatcher.CommandContext;

public class AlwaysFailingCondition implements CommandCondition<Object> {

    private AlwaysFailingCondition() {}

    public static CommandCondition<Object> fail() {
        return new AlwaysFailingCondition();
    }

    @Override
    public void test(final CommandContext<Object> context) throws UnfulfilledConditionException {
        throw new UnfulfilledConditionException(this);
    }
}
