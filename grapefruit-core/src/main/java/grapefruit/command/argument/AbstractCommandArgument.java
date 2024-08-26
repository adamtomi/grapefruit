package grapefruit.command.argument;

import grapefruit.command.binding.BindingKey;

import static java.util.Objects.requireNonNull;

abstract class AbstractCommandArgument implements CommandArgument {
    protected final String name;
    protected final BindingKey<?> key;
    protected final boolean isFlag;

    AbstractCommandArgument(String name, BindingKey<?> key, boolean isFlag) {
        this.name = requireNonNull(name, "name cannot be null");
        this.key = requireNonNull(key, "key cannot be null");
        this.isFlag = isFlag;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public BindingKey<?> key() {
        return this.key;
    }

    @Override
    public boolean isFlag() {
        return this.isFlag;
    }
}
