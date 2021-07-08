package grapefruit.command;

import grapefruit.command.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class CommandException extends Exception {
    @Serial
    private static final long serialVersionUID = 5902076308885496439L;
    private final Message message;

    public CommandException(final @Nullable Throwable cause, final @NotNull Message message) {
        super(cause);
        this.message = requireNonNull(message, "message cannot be null");
    }

    public CommandException(final @NotNull Message message) {
        this(null, message);
    }

    public @NotNull Message message() {
        return this.message;
    }
}
