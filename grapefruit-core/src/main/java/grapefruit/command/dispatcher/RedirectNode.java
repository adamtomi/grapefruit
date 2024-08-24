package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

record RedirectNode(@NotNull String route, @NotNull String[] arguments) {}
