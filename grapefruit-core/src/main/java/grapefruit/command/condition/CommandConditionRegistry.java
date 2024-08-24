package grapefruit.command.condition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;

import static grapefruit.command.condition.CommandCondition.VALID_NAME_PATTERN;
import static java.lang.String.format;

public final class CommandConditionRegistry<S> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, CommandCondition<S>> conditions = new HashMap<>();

    public void registerCondition(final @NotNull CommandCondition<S> condition) {
        try {
            this.lock.lock();
            final String id = condition.id();
            if (this.conditions.containsKey(id)) {
                throw new IllegalStateException(format("Condition with id '%s' already registered", id));
            }

            final Matcher idMatcher = VALID_NAME_PATTERN.matcher(id);
            if (!idMatcher.matches()) {
                throw new IllegalArgumentException(format("Condition id '%s' is invalid, must match '%s'", id, VALID_NAME_PATTERN.pattern()));
            }

            this.conditions.put(id, condition);
        } finally {
            this.lock.unlock();
        }
    }

    public @NotNull Optional<CommandCondition<S>> findCondition(final @NotNull String id) {
        try {
            this.lock.lock();
            final @Nullable CommandCondition<S> condition = this.conditions.get(id);
            return Optional.ofNullable(condition);
        } finally {
            this.lock.unlock();
        }
    }
}
