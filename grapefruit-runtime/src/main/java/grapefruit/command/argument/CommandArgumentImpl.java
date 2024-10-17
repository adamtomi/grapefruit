package grapefruit.command.argument;

import grapefruit.command.argument.binding.BoundArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.argument.modifier.ModifierChain;
import grapefruit.command.util.key.Key;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

abstract class CommandArgumentImpl<T> implements CommandArgument<T> {
    protected final String name;
    protected final Key<T> key;
    protected final Key<T> mapperKey;
    protected final boolean isFlag;
    protected final ModifierChain<T> modifierChain;

    CommandArgumentImpl(String name, Key<T> key, Key<T> mapperKey, boolean isFlag, ModifierChain<T> modifierChain) {
        this.name = requireNonNull(name, "name cannot be null");
        this.key = requireNonNull(key, "key cannot be null");
        this.mapperKey = requireNonNull(mapperKey, "mapperKey cannot be null");
        this.isFlag = isFlag;
        this.modifierChain = requireNonNull(modifierChain, "modifierChain cannot be null");
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
    public boolean isFlag() {
        return this.isFlag;
    }

    @Override
    public ModifierChain<T> modifierChain() {
        return this.modifierChain;
    }

    @Override
    public BoundArgument<T> bind(ArgumentMapper<T> mapper) {
        return BoundArgument.of(this, mapper);
    }

    static final class Required<T> extends CommandArgumentImpl<T> {
        Required(String name, Key<T> key, Key<T> mapperKey, ModifierChain<T> modifierChain) {
            super(name, key, mapperKey, false, modifierChain);
        }

        @Override
        public FlagArgument<T> asFlag() {
            throw new UnsupportedOperationException("Attempted to cast a non-flag argument");
        }

        @Override
        public String toString() {
            return "CommandArgumentImpl$Required(name=%s)".formatted(this.name);
        }
    }

    static abstract class Flag<T> extends CommandArgumentImpl<T> implements FlagArgument<T> {
        private final char shorthand;
        private final boolean isPresenceFlag;

        Flag(String name, Key<T> key, Key<T> mapperKey, char shorthand, boolean isPresenceFlag, ModifierChain<T> modifierChain) {
            super(name, key, mapperKey, true, modifierChain);
            this.shorthand = shorthand;
            this.isPresenceFlag = isPresenceFlag;
        }

        @Override
        public char shorthand() {
            return this.shorthand;
        }

        @Override
        public boolean isPresenceFlag() {
            return this.isPresenceFlag;
        }

        @Override
        public FlagArgument<T> asFlag() {
            return this;
        }

        @Override
        public String toString() {
            return "CommandArgumentImpl$Flag(name=%s, shorthand=%s, isPresenceFlag=%b)"
                    .formatted(this.name, this.shorthand, this.isPresenceFlag);
        }
    }

    static final class PresenceFlag extends Flag<Boolean> {
        /* Store a mapper that always returns the value "true" */
        private static final ArgumentMapper<Boolean> BOOLEAN_MAPPER = ArgumentMapper.constant(true);
        /*
         * Dummy key, we are not going to use this, since presence flag values are
         * determined by whether the flag has been set or not.
         */
        private static final Key<Boolean> BOOLEAN_KEY = Key.of(Boolean.class);

        PresenceFlag(String name, Key<Boolean> key, char shorthand, ModifierChain<Boolean> modifierChain) {
            super(name, key, BOOLEAN_KEY, shorthand, true, modifierChain);
        }

        @Override
        public BoundArgument<Boolean> bind(Function<Key<Boolean>, ArgumentMapper<Boolean>> mapperAccess) {
            // Since this is a presence flag, we always want to bind to BOOLEAN_MAPPER
            return bind(BOOLEAN_MAPPER);
        }
    }

    static final class ValueFlag<T> extends Flag<T> {
        ValueFlag(String name, Key<T> key, Key<T> mapperKey, char shorthand, ModifierChain<T> modifierChain) {
            super(name, key, mapperKey, shorthand, false, modifierChain);
        }
    }
}
