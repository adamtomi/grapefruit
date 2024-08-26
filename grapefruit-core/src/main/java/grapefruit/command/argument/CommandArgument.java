package grapefruit.command.argument;

import grapefruit.command.binding.BindingKey;

public interface CommandArgument {

    String name();

    BindingKey<?> key();

    boolean isFlag();

    // TODO store qualifiers
}
