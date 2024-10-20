package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.CommandException;

import java.io.Serial;

/**
 * This exception is thrown, when a command handler method throws an exception, which is
 * caught by its wrapper {@link CommandDefinition} instance. The original exception will
 * be set as the cause of this exception.
 */
public class CommandInvocationException extends CommandException {
    @Serial
    private static final long serialVersionUID = 5619124383975996166L;

    public CommandInvocationException(Throwable cause) {
        super(cause);
    }
}
