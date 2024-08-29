package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;

import java.io.Serial;

public class CommandInvocationException extends CommandException {
    @Serial
    private static final long serialVersionUID = 5619124383975996166L;

    public CommandInvocationException(Throwable cause) {
        super(cause);
    }
}
