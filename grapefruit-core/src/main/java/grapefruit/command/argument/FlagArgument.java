package grapefruit.command.argument;

import grapefruit.command.util.key.Key;

public interface FlagArgument extends CommandArgument {

    char shorthand();

    boolean isPresenceFlag();

    static FlagArgument presence(String name, char shorthand) {
        return new StandardFlagArgument(name, Key.of(Boolean.TYPE), shorthand, true);
    }

    static FlagArgument value(String name, char shorthand, Key<?> key) {
        return new StandardFlagArgument(name, key, shorthand, false);
    }
}
