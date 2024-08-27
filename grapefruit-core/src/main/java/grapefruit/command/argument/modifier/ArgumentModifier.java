package grapefruit.command.argument.modifier;

import grapefruit.command.argument.CommandArgumentException;

@FunctionalInterface
public interface ArgumentModifier<T> {

    T apply(T input) throws CommandArgumentException;
}
