package grapefruit.command.argument;

import grapefruit.command.binding.BindingKey;

public final class StandardCommandArgument extends AbstractCommandArgument {

    public StandardCommandArgument(String name, BindingKey<?> key) {
        super(name, key, false);
    }

    @Override
    public String toString() {
        return "StandardCommandArgument(name=%s)".formatted(this.name);
    }
}
