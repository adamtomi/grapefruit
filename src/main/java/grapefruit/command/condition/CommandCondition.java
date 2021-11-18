package grapefruit.command.condition;

import grapefruit.command.dispatcher.CommandContext;
import org.jetbrains.annotations.NotNull;

public interface CommandCondition<S> {

    @NotNull String id();

    void test(final @NotNull CommandContext<S> context) throws ConditionFailedException;
}
