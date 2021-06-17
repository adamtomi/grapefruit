package io.grapefruit.command;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public class CommandException extends Exception {
    @Serial
    private static final long serialVersionUID = 5902076308885496439L;

    public CommandException() {
        super();
    }

    public CommandException(final @NotNull Throwable cause) {
        super(cause);
    }
}
