package io.grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandAuthorizer<S> {

    boolean isAuthorized(final @NotNull S source, final @NotNull String permission);
}
