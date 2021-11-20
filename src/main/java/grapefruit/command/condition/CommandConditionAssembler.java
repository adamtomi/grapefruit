package grapefruit.command.condition;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class CommandConditionAssembler<S> {
    private final CommandConditionRegistry<S> conditionRegistry;

    public CommandConditionAssembler(final @NotNull CommandConditionRegistry<S> conditionRegistry) {
        this.conditionRegistry = requireNonNull(conditionRegistry, "conditionRegistry cannot be null");
    }

    public @NotNull Optional<CommandCondition<S>> constructCondition(final @NotNull Method method) {
        if (!method.isAnnotationPresent(Condition.class)) {
            return Optional.empty();
        }

        final Condition[] conditions = method.getAnnotationsByType(Condition.class);
        final List<CommandCondition<S>> orConditions = Arrays.stream(conditions)
                .map(this::parseAnnotation)
                .toList();
        return Optional.of(new AndCondition<>(orConditions));
    }

    private @NotNull CommandCondition<S> parseAnnotation(final @NotNull Condition def) {
        final List<CommandCondition<S>> conditions = new ArrayList<>();
        for (final String each : def.value()) {
            final CommandCondition<S> condition = this.conditionRegistry.findCondition(each)
                    .orElseThrow(() -> new IllegalArgumentException(format("Could not find condition with id '%s'", each)));
            conditions.add(condition);
        }

        return new OrCondition<>(conditions);
    }
}
