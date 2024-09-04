package grapefruit.command.argument;

import grapefruit.command.argument.chain.BoundArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.util.key.Key;

public final class StandardCommandArgument<T> extends AbstractCommandArgument<T> {

    public StandardCommandArgument(String name, Key<T> key) {
        super(name, key, false);
    }

    @Override
    public BoundArgument<T, ?> bind(ArgumentMapper<T> mapper) {
        return BoundArgument.arg(this, mapper);
    }

    @Override
    public String toString() {
        return "StandardCommandArgument(name=%s)".formatted(this.name);
    }
}
