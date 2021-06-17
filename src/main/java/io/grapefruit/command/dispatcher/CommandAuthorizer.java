package io.grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandAuthorizer<S> {
    CommandAuthorizer<?> NO_OP = (source, permission) -> true;

    boolean isAuthorized(final @NotNull S source, final @NotNull String permission);
}
