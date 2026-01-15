package grapefruit.command.argument;

import grapefruit.command.argument.condition.CommandCondition;
import grapefruit.command.argument.condition.UnfulfilledConditionException;
import grapefruit.command.mock.NilCommandContext;
import org.junit.jupiter.api.Test;

import static grapefruit.command.argument.condition.CommandCondition.and;
import static grapefruit.command.argument.condition.CommandCondition.or;
import static grapefruit.command.mock.AlwaysCondition.fail;
import static grapefruit.command.mock.AlwaysCondition.pass;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandConditionTests {

    @Test
    public void and_anyFails() {
        final CommandCondition<Object> condition = and(fail(), fail(), pass());
        assertThrows(UnfulfilledConditionException.class, () -> condition.testEarly(new NilCommandContext()));
    }

    @Test
    public void and_allFail() {
        final CommandCondition<Object> condition = and(fail(), fail(), fail());
        assertThrows(UnfulfilledConditionException.class, () -> condition.testEarly(new NilCommandContext()));
    }

    @Test
    public void and_allPass() {
        final CommandCondition<Object> condition = and(pass(), pass(), pass());
        assertDoesNotThrow(() -> condition.testEarly(new NilCommandContext()));
    }

    @Test
    public void or_anyFails() {
        final CommandCondition<Object> condition = or(fail(), pass(), fail());
        assertDoesNotThrow(() -> condition.testEarly(new NilCommandContext()));
    }

    @Test
    public void or_allFail() {
        final CommandCondition<Object> condition = or(fail(), fail(), fail());
        assertThrows(UnfulfilledConditionException.class, () -> condition.testEarly(new NilCommandContext()));
    }

    @Test
    public void or_allPass() {
        final CommandCondition<Object> condition = or(pass(), pass(), pass());
        assertDoesNotThrow(() -> condition.testEarly(new NilCommandContext()));
    }
}
