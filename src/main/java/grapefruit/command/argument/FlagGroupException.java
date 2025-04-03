package grapefruit.command.argument;

import java.io.Serial;

public class FlagGroupException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = 8300222690822649697L;
    private final char shorthand;

    public FlagGroupException(
            final String consumed,
            final String argument,
            final String remaining,
            final char shorthand
    ) {
        super(consumed, argument, remaining);
        this.shorthand = shorthand;
    }

    public char shorthand() {
        return this.shorthand;
    }
}
