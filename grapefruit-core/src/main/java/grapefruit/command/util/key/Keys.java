package grapefruit.command.util.key;

import com.google.common.reflect.TypeToken;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class Keys {

    private Keys() {}

    static final class KeyImpl<T> implements Key<T> {
        private final TypeToken<T> type;

        KeyImpl(TypeToken<T> type) {
            this.type = requireNonNull(type, "type cannot be null");
        }

        @Override
        public TypeToken<T> type() {
            return this.type;
        }

        @Override
        public String toString() {
            return "KeyImpl(type=%s)".formatted(this.type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyImpl<?> key = (KeyImpl<?>) o;
            return Objects.equals(this.type, key.type);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.type);
        }
    }

    static final class NamedKeyImpl<T> implements Key.Named<T> {
        private final TypeToken<T> type;
        private final String name;

        NamedKeyImpl(TypeToken<T> type, String name) {
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
            return "NamedKeyImpl(type=%s, name=%s)".formatted(this.type, this.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NamedKeyImpl<?> namedKey = (NamedKeyImpl<?>) o;
            return Objects.equals(this.type, namedKey.type) && Objects.equals(this.name, namedKey.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.name);
        }
    }
}
