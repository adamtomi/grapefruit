package grapefruit.command.argument.condition;

import grapefruit.command.dispatcher.CommandContext;

import java.util.List;

public interface CommandCondition<S> {

    void testEarly(final CommandContext<S> context) throws UnfulfilledConditionException;

    void testLate(final CommandContext<S> context) throws UnfulfilledConditionException;

    interface Early<S> extends CommandCondition<S> {
        @Override
        default void testLate(final CommandContext<S> context) {}
    }

    interface Late<S> extends CommandCondition<S> {
        @Override
        default void testEarly(final CommandContext<S> context) {}
    }

    @SafeVarargs
    static <S> CommandCondition<S> and(final CommandCondition<S>... conditions) {
        return new AndCondition<>(List.of(conditions));
    }

    @SafeVarargs
    static <S> CommandCondition<S> or(final CommandCondition<S>... conditions) {
        return new OrCondition<>(List.of(conditions));
    }
}
