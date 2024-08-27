package grapefruit.command.argument;

import grapefruit.command.util.key.Key;

public interface FlagArgument<T> extends CommandArgument<T> {

    char shorthand();

    boolean isPresenceFlag();

    static FlagArgument<Boolean> presence(String name, char shorthand) {
        return new StandardFlagArgument<>(name, Key.of(Boolean.TYPE), shorthand, true);
    }

    static <T> FlagArgument<T> value(String name, char shorthand, Key<T> key) {
        return new StandardFlagArgument<>(name, key, shorthand, false);
    }
}
