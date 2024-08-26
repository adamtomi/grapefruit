package grapefruit.command.argument;

import grapefruit.command.binding.BindingKey;

public interface FlagArgument extends CommandArgument {

    char shorthand();

    boolean isPresenceFlag();

    static FlagArgument presence(String name, char shorthand) {
        return new StandardFlagArgument(name, BindingKey.of(Boolean.TYPE), shorthand, true);
    }

    static FlagArgument value(String name, char shorthand, BindingKey<?> key) {
        return new StandardFlagArgument(name, key, shorthand, false);
    }
}
