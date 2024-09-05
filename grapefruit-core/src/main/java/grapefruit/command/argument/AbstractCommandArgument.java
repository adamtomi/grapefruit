package grapefruit.command.argument;

import grapefruit.command.util.key.Key;

import static java.util.Objects.requireNonNull;

abstract class AbstractCommandArgument<T> implements CommandArgument<T> {
    protected final String name;
    protected final Key<T> key;
    protected final Key<T> mapperKey;
    protected final boolean isFlag;

    AbstractCommandArgument(String name, Key<T> key, Key<T> mapperKey, boolean isFlag) {
        this.name = requireNonNull(name, "name cannot be null");
        this.key = requireNonNull(key, "key cannot be null");
        this.mapperKey = requireNonNull(mapperKey, "mapperKey cannot be null");
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
    public Key<T> mapperKey() {
        return this.mapperKey;
    }

    @Override
    public boolean isFlag() {
        return this.isFlag;
    }
}
