package io.grapefruit.command.dispatcher.exception;

import io.grapefruit.command.CommandException;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class NoSuchCommandException extends CommandException {
    @Serial
    private static final long serialVersionUID = 514631540629935937L;
    private final String rootCommand;
    private final String commandLine;

    public NoSuchCommandException(final @NotNull String rootCommand,
                                  final @NotNull String commandLine) {
        super();
        this.rootCommand = requireNonNull(rootCommand, "rootCommand cannot be null");
        this.commandLine = requireNonNull(commandLine, "commandLine cannot be null");
    }

    public @NotNull String rootCommand() {
        return this.rootCommand;
    }

    public @NotNull String commandLine() {
        return this.commandLine;
    }
}
