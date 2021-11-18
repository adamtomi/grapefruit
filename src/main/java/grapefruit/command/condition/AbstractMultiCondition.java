package grapefruit.command.condition;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractMultiCondition<S> implements CommandCondition<S> {
    private static final String ID = "or:%s";
    protected final List<CommandCondition<S>> conditions;
    private final String id;

    AbstractMultiCondition(final @NotNull List<CommandCondition<S>> conditions) {
        this.conditions = conditions;
        this.id = ID.formatted(conditions.stream()
                .map(CommandCondition::id)
                .collect(Collectors.joining(";")));
    }

    @Override
    public final @NotNull String id() {
        return this.id;
    }
}
