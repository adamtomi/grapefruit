package grapefruit.command;

import java.io.Serial;

public class CommandException extends Exception {
    @Serial
    private static final long serialVersionUID = -3461202024417516177L;

    public CommandException() {
        super();
    }

    public CommandException(final Throwable cause) {
        super(cause);
    }
}
