package grapefruit.command.argument;

import grapefruit.command.argument.modifier.ArgumentModifier;
import grapefruit.command.util.key.Key;

import java.util.Set;

public interface CommandArgument<T> {

    String name();

    Key<T> key();

    boolean isFlag();

    default Set<ArgumentModifier<T>> modifiers() {
        return Set.of();
    }
}
