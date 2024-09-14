package grapefruit.command.argument;

import grapefruit.command.argument.binding.BoundArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.argument.modifier.ModifierChain;
import grapefruit.command.util.key.Key;

import static java.util.Objects.requireNonNull;

abstract class AbstractCommandArgument<T> implements CommandArgument<T> {
    protected final String name;
    protected final Key<T> key;
    protected final Key<T> mapperKey;
    protected final boolean isFlag;
    protected final ModifierChain<T> modifierChain;

    AbstractCommandArgument(String name, Key<T> key, Key<T> mapperKey, boolean isFlag, ModifierChain<T> modifierChain) {
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

    static final class Required<T> extends AbstractCommandArgument<T> {
        Required(String name, Key<T> key, Key<T> mapperKey, ModifierChain<T> modifierChain) {
            super(name, key, mapperKey, false, modifierChain);
        }

        @Override
        public FlagArgument<T> asFlag() {
            throw new UnsupportedOperationException("Attempted to cast a non-flag argument");
        }

        @Override
        public String toString() {
            return "AbstractCommandArgument$Required(name=%s)".formatted(this.name);
        }
    }

    static final class Flag<T> extends AbstractCommandArgument<T> implements FlagArgument<T> {
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
            return "AbstractCommandArgument$Flag(name=%s, shorthand=%s, isPresenceFlag=%b)"
                    .formatted(this.name, this.shorthand, this.isPresenceFlag);
        }
    }
}
