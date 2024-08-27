package grapefruit.command.argument;

import grapefruit.command.util.key.Key;

public final class StandardCommandArgument extends AbstractCommandArgument {

    public StandardCommandArgument(String name, Key<?> key) {
        super(name, key, false);
    }

    @Override
    public String toString() {
        return "StandardCommandArgument(name=%s)".formatted(this.name);
    }
}
