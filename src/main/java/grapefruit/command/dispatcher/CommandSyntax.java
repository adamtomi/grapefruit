package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CommandSyntax (@NotNull String rawSyntax, @NotNull List<String> syntaxOptions) {}
