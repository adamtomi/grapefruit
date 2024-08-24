package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class MessageKeyImpl implements MessageKey {
    private final String key;

    MessageKeyImpl(final @NotNull String key) {
        this.key = requireNonNull(key, "key cannot be null");
    }

    @Override
    public @NotNull String key() {
        return this.key;
    }

    @Override
    public @NotNull String toString() {
        return "MessageKeyImpl[" + this.key + ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MessageKeyImpl that = (MessageKeyImpl) o;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key);
    }
}
