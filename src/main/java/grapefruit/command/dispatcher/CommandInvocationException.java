package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;

import java.io.Serial;

public class CommandInvocationException extends CommandException {
    @Serial
    private static final long serialVersionUID = 3033014139998396572L;

    public CommandInvocationException(final Throwable cause) {
        super(cause);
    }
}
