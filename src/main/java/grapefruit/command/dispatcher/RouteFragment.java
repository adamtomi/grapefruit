package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

public record RouteFragment (@NotNull String primary, @NotNull String[] aliases) {}