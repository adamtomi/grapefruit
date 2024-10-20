package grapefruit.command.runtime.generated;

import grapefruit.command.runtime.argument.modifier.ModifierBlueprint;
import grapefruit.command.runtime.util.key.Key;

import java.util.List;

import static java.util.Objects.requireNonNull;

class ArgumentMirrorImpl<T> implements ArgumentMirror<T> {
    protected final String name;
    protected final Key<T> key;
    protected final Key<T> mapperKey;
    protected final List<ModifierBlueprint> modifiers;

    ArgumentMirrorImpl(
            String name,
            Key<T> key,
            Key<T> mapperKey,
            List<ModifierBlueprint> modifiers
    ) {
        this.name = requireNonNull(name, "name cannot be null");
        this.key = requireNonNull(key, "key cannot be null");
        this.mapperKey = requireNonNull(mapperKey, "mapperKey cannot be null");
        this.modifiers = requireNonNull(modifiers, "modifiers cannot be null");
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Key<T> key() {
        return this.key;
    }

    @Override
    public Key<T> mapperKey() {
        return this.mapperKey;
    }

    @Override
    public List<ModifierBlueprint> modifiers() {
        return List.copyOf(this.modifiers);
    }

    @Override
    public String toString() {
        return "ArgumentMirrorImpl(name=%s, key=%s, mapperKey=%s, modifiers=%s)".formatted(
                this.name, this.key, this.mapperKey, this.modifiers
        );
    }

    static class Flag <T> extends ArgumentMirrorImpl<T> implements ArgumentMirror.Flag<T> {
        private final char shorthand;

        Flag(
                String name,
                char shorthand,
                Key<T> key,
                Key<T> mapperKey,
                List<ModifierBlueprint> modifiers
        ) {
            super(name, key, mapperKey, modifiers);
            this.shorthand = shorthand;
        }

        @Override
        public char shorthand() {
            return this.shorthand;
        }

        @Override
        public String toString() {
            return "ArgumentMirrorImpl$Flag(name=%s, key=%s, mapperKey=%s, modifiers=%s, shorthand=%s)".formatted(
                    this.name, this.key, this.mapperKey, this.modifiers, this.shorthand
            );
        }
    }
}
