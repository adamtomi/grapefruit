package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class MessageImpl implements Message {
    private final MessageKey key;
    private final List<Template> templates;

    MessageImpl(final @NotNull MessageKey key, final @NotNull List<Template> templates) {
        this.key = requireNonNull(key, "key cannot be null");
        this.templates = requireNonNull(templates, "templates cannot be null");
    }

    @Override
    public @NotNull MessageKey key() {
        return this.key;
    }

    @Override
    public @NotNull List<Template> templates() {
        return this.templates;
    }

    @Override
    public @NotNull String get(final @NotNull MessageProvider provider) {
        String result = provider.provide(this.key);
        for (final Template template : this.templates) {
            result = result.replace(template.placeholder(), template.replacement());
        }

        return result;
    }

    @Override
    public @NotNull String toString() {
        return "MessageImpl[" +
                "key=" + this.key +
                ", templates=" + this.templates +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MessageImpl message = (MessageImpl) o;
        return Objects.equals(this.key, message.key) && Objects.equals(this.templates, message.templates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.templates);
    }
}
