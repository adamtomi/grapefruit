package grapefruit.command.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BooleanFunction<T> {

    boolean apply(final @NotNull T t);
}
