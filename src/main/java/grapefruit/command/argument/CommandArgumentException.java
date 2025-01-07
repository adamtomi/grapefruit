package grapefruit.command.argument;

import grapefruit.command.CommandException;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class CommandArgumentException extends CommandException {
    @Serial
    private static final long serialVersionUID = -8437065396865235634L;
    private final String consumed;
    private final String argument;
    private final String remaining;

    public CommandArgumentException(final @Nullable CommandException cause, final String consumed, final String argument, final String remaining) {
        super(cause);
        this.consumed = requireNonNull(consumed, "consumed cannot be null");
        this.argument = requireNonNull(argument, "argument cannot be null");
        this.remaining = requireNonNull(remaining, "remaining cannot be null");
    }

    public CommandArgumentException(final String consumed, final String argument, final String remaining) {
        this(null, consumed, argument, remaining);
    }

    public String consumed() {
        return this.consumed;
    }

    public String argument() {
        return this.argument;
    }

    public String remaining() {
        return this.remaining;
    }
}
