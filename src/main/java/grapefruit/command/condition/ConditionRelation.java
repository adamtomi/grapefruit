package grapefruit.command.condition;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface ConditionRelation<S> {

    @NotNull CommandCondition<S> generateCondition(final @NotNull List<CommandCondition<S>> conditions);
}
