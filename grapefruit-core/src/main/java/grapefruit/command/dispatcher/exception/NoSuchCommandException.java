package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class NoSuchCommandException extends CommandException {
    @Serial
    private static final long serialVersionUID = 514631540629935937L;
    private final String command;

    public NoSuchCommandException(final @NotNull String command) {
        super(Message.of(
                MessageKeys.NO_SUCH_COMMAND,
                Template.of("{name}", requireNonNull(command, "command cannot be null"))
        ));
        this.command = command;
    }

    public @NotNull String command() {
        return this.command;
    }
}
