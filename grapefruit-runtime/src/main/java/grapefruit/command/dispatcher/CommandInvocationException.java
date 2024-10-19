package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.annotation.Command;

import java.io.Serial;

/**
 * This exception is thrown, when a command handler method (annotated with {@link Command})
 * throws an exception, which is caught by its wrapper {@link grapefruit.command.Command}'s {@link grapefruit.command.Command#run(CommandContext)}
 * method. The original exception will be set as the cause of this exception.
 */
public class CommandInvocationException extends CommandException {
    @Serial
    private static final long serialVersionUID = 5619124383975996166L;

    public CommandInvocationException(Throwable cause) {
        super(cause);
    }
}
