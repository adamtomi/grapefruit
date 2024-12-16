package grapefruit.command.util.key;

import grapefruit.command.util.ToStringer;
import io.leangen.geantyref.TypeToken;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class KeyImpl<T> implements Key<T> {
    private final TypeToken<T> type;
    private final String name;

    KeyImpl(final TypeToken<T> type, final String name) {
        this.type = requireNonNull(type, "type cannot be null");
        this.name = requireNonNull(name, "name cannot be null");
    }

    @Override
    public TypeToken<T> type() {
        return this.type;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("type", this.type)
                .append("name", this.name)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final KeyImpl<?> key = (KeyImpl<?>) o;
        return Objects.equals(this.type, key.type) && Objects.equals(this.name, key.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.name);
    }
}
