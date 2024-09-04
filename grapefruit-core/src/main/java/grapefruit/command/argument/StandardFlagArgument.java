package grapefruit.command.argument;

import grapefruit.command.argument.chain.BoundArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.util.key.Key;

final class StandardFlagArgument<T> extends AbstractCommandArgument<T> implements FlagArgument<T> {
    private final char shorthand;
    private final boolean isPresenceFlag;

    public StandardFlagArgument(String name, Key<T> key, char shorthand, boolean isPresenceFlag) {
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
    public BoundArgument<T, ?> bind(ArgumentMapper<T> mapper) {
        return BoundArgument.flag(this, mapper);
    }

    @Override
    public String toString() {
        return "StandardFlagArgument(name=%s, shorthand=%s, isPresenceFlag=%b)"
                .formatted(this.name, this.shorthand, this.isPresenceFlag);
    }
}
