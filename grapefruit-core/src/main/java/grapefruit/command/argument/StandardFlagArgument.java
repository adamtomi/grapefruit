package grapefruit.command.argument;

import grapefruit.command.util.key.Key;

final class StandardFlagArgument extends AbstractCommandArgument implements FlagArgument {
    private final char shorthand;
    private final boolean isPresenceFlag;

    public StandardFlagArgument(String name, Key<?> key, char shorthand, boolean isPresenceFlag) {
        super(name, key, true);
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
    public String toString() {
        return "StandardFlagArgument(name=%s, shorthand=%s, isPresenceFlag=%b)"
                .formatted(this.name, this.shorthand, this.isPresenceFlag);
    }
}
