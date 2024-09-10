package grapefruit.command.argument;

import grapefruit.command.util.key.Key;

/**
 * Utility class to easily create command arguments.
 */
public final class CommandArguments {

    private CommandArguments() {}

    /**
     * Creates a standard (required, positional) argument.
     *
     * @param name The name of the argument
     * @param key The Key of the argument
     * @param mapperKey The mapperKey of the argument
     * @return The created argument
     */
    public static <T> CommandArgument<T> standard(String name, Key<T> key, Key<T> mapperKey) {
        return new Standard<>(name, key, mapperKey);
    }

    /**
     * Creates a new presence flag argument. Presence flags
     * always represent boolean values; the flag being set
     * equals to true, while omitting the flag results in
     * its value being set to false.
     *
     * @param name The name of the argument
     * @param shorthand The flag shorthand
     * @param mapperKey The mapper key
     * @return the created argument
     */
    public static FlagArgument<Boolean> presenceFlag(String name, char shorthand, Key<Boolean> mapperKey) {
        return new Flag<>(name, Key.of(Boolean.class), mapperKey, shorthand, true);
    }

    /**
     * Creates a new value flag argument.
     *
     * @param name The name of the argument
     * @param shorthand The flag shorthand
     * @param key The key of the argument
     * @param mapperKey The mapper key
     * @return The created argument
     */
    public static <T> FlagArgument<T> valueFlag(String name, char shorthand, Key<T> key, Key<T> mapperKey) {
        return new Flag<>(name, key, mapperKey, shorthand, false);
    }

    private static final class Standard<T> extends AbstractCommandArgument<T> {
        private Standard(String name, Key<T> key, Key<T> mapperKey) {
            super(name, key, mapperKey, false);
        }

        @Override
        public FlagArgument<T> asFlag() {
            throw new UnsupportedOperationException("Attempted to cast a non-flag argument");
        }

        @Override
        public String toString() {
            return "CommandArguments$Standard(name=%s)".formatted(this.name);
        }
    }

    private static final class Flag<T> extends AbstractCommandArgument<T> implements FlagArgument<T> {
        private final char shorthand;
        private final boolean isPresenceFlag;

        public Flag(String name, Key<T> key, Key<T> mapperKey, char shorthand, boolean isPresenceFlag) {
            super(name, key, mapperKey, true);
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
            return "CommandArguments.Flag(name=%s, shorthand=%s, isPresenceFlag=%b)"
                    .formatted(this.name, this.shorthand, this.isPresenceFlag);
        }
    }
}
