package grapefruit.command.runtime.argument;

import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.argument.modifier.ArgumentModifier;
import grapefruit.command.runtime.util.key.Key;

import java.util.List;

import static java.util.Objects.requireNonNull;

abstract class CommandArgumentImpl<T> implements CommandArgument<T> {
    protected final String name;
    protected final Key<T> key;
    private final ArgumentMapper<T> mapper;
    private final List<ArgumentModifier<T>> modifiers;
    protected final boolean isFlag;

    CommandArgumentImpl(String name, Key<T> key, ArgumentMapper<T> mapper, List<ArgumentModifier<T>> modifiers, boolean isFlag) {
        this.name = requireNonNull(name, "name cannot be null");
        this.key = requireNonNull(key, "key cannot be null");
        this.mapper = requireNonNull(mapper, "mapper cannot be null");
        this.modifiers = requireNonNull(modifiers, "modifiers cannot be null");
        this.isFlag = isFlag;
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
    public ArgumentMapper<T> mapper() {
        return this.mapper;
    }

    @Override
    public List<ArgumentModifier<T>> modifiers() {
        return List.copyOf(this.modifiers);
    }

    @Override
    public boolean isFlag() {
        return this.isFlag;
    }

    static final class Required<T> extends CommandArgumentImpl<T> {
        Required(String name, Key<T> key, ArgumentMapper<T> mapper, List<ArgumentModifier<T>> modifiers) {
            super(name, key, mapper, modifiers, false);
        }

        @Override
        public CommandArgument.Flag<T> asFlag() {
            throw new UnsupportedOperationException("Attempted to cast a non-flag argument");
        }

        @Override
        public String toString() {
            return "CommandArgumentImpl$Required(name=%s)".formatted(this.name);
        }
    }

    static abstract class Flag<T> extends CommandArgumentImpl<T> implements CommandArgument.Flag<T> {
        private final char shorthand;
        private final boolean isPresence;

        Flag(String name, char shorthand, Key<T> key, ArgumentMapper<T> mapper, List<ArgumentModifier<T>> modifiers, boolean isPresence) {
            super(name, key, mapper, modifiers, true);
            this.shorthand = shorthand;
            this.isPresence = isPresence;
        }

        @Override
        public char shorthand() {
            return this.shorthand;
        }

        @Override
        public boolean isPresence() {
            return this.isPresence;
        }

        @Override
        public CommandArgument.Flag<T> asFlag() {
            return this;
        }

        @Override
        public String toString() {
            return "CommandArgumentImpl$Flag(name=%s, shorthand=%s, isPresence=%b)"
                    .formatted(this.name, this.shorthand, this.isPresence);
        }
    }

    static final class PresenceFlag extends Flag<Boolean> {
        /*
         * Presence flags resolve to true if set, otherwise false.
         */
        private static final ArgumentMapper<Boolean> MAPPER = ArgumentMapper.constant(true);

        PresenceFlag(String name, char shorthand, Key<Boolean> key) {
            super(name, shorthand, key, MAPPER, List.of(), true);
        }
    }

    static final class ValueFlag<T> extends Flag<T> {
        ValueFlag(String name, char shorthand, Key<T> key, ArgumentMapper<T> mapper, List<ArgumentModifier<T>> modifiers) {
            super(name, shorthand, key, mapper, modifiers, false);
        }
    }

    static final class RequiredBuilder<T> implements CommandArgument.RequiredBuilder<T> {
        private final String name;
        private final Key<T> key;
        private ArgumentMapper<T> mapper;
        private List<ArgumentModifier<T>> modifiers;

        RequiredBuilder(String name, Key<T> key) {
            this.name = requireNonNull(name, "name cannot be null");
            this.key = requireNonNull(key, "key cannot be null");
        }

        @Override
        public RequiredBuilder<T> withMapper(ArgumentMapper<T> mapper) {
            this.mapper = mapper;
            return this;
        }

        @Override
        public RequiredBuilder<T> withModifiers(List<ArgumentModifier<T>> modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        @Override
        public CommandArgument<T> build() {
            return new Required<>(this.name, this.key, this.mapper, this.modifiers);
        }
    }

    static final class PresenceFlagBuilder implements CommandArgument.PresenceFlagBuilder {
        private final String name;
        private final Key<Boolean> key;
        private char shorthand;

        PresenceFlagBuilder(String name, Key<Boolean> key) {
            this.name = requireNonNull(name, "name cannot be null");
            this.key = requireNonNull(key, "key cannot be null");
        }

        @Override
        public PresenceFlagBuilder shorthand(char shorthand) {
            this.shorthand = shorthand;
            return this;
        }

        @Override
        public CommandArgument.Flag<Boolean> build() {
            return new PresenceFlag(this.name, this.shorthand, this.key);
        }
    }

    static final class ValueFlagBuilder<T> implements CommandArgument.ValueFlagBuilder<T> {
        private final String name;
        private final Key<T> key;
        private ArgumentMapper<T> mapper;
        private List<ArgumentModifier<T>> modifiers;
        private char shorthand;

        ValueFlagBuilder(String name, Key<T> key) {
            this.name = requireNonNull(name, "name cannot be null");
            this.key = requireNonNull(key, "key cannot be null");
        }

        @Override
        public ValueFlagBuilder<T> withMapper(ArgumentMapper<T> mapper) {
            this.mapper = mapper;
            return this;
        }

        @Override
        public ValueFlagBuilder<T> withModifiers(List<ArgumentModifier<T>> modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        @Override
        public ValueFlagBuilder<T> shorthand(char shorthand) {
            this.shorthand = shorthand;
            return this;
        }

        @Override
        public CommandArgument.Flag<T> build() {
            return new ValueFlag<>(this.name, this.shorthand, this.key, this.mapper, this.modifiers);
        }
    }
}
