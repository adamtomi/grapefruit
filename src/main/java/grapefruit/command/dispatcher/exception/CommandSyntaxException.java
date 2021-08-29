package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public class CommandSyntaxException extends CommandException {
    @Serial
    private static final long serialVersionUID = 8701865850834783428L;

    public CommandSyntaxException(final @NotNull Message message) {
        super(null, message);
    }
}
