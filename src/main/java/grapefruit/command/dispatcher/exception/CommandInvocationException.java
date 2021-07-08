package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public class CommandInvocationException extends CommandException {
    @Serial
    private static final long serialVersionUID = -6401148203774906350L;

    public CommandInvocationException(final @NotNull Throwable cause, final @NotNull String commandLine) {
        super(cause, Message.of(
                MessageKeys.FAILED_TO_EXECUTE_COMMAND,
                Template.of("{commandline}", commandLine)
        ));
    }
}
