package grapefruit.command.argument;

import grapefruit.command.util.key.Key;

/**
 * Utility class to easily create command arguments.
 */
public final class CommandArguments {

    private CommandArguments() {}

    /**
     * Creates a required command arguments.
     *
     * @param name The name of the argument
     * @param key The Key of the argument
     * @param mapperKey The mapperKey of the argument
     * @return The created argument
     */
    public static <T> CommandArgument<T> required(String name, Key<T> key, Key<T> mapperKey) {
        return new AbstractCommandArgument.Required<>(name, key, mapperKey);
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
        return new AbstractCommandArgument.Flag<>(name, Key.of(Boolean.class), mapperKey, shorthand, true);
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
        return new AbstractCommandArgument.Flag<>(name, key, mapperKey, shorthand, false);
    }
}
