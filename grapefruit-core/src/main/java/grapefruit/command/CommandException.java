package grapefruit.command;

import java.io.Serial;

public class CommandException extends Exception {
    @Serial
    private static final long serialVersionUID = -1522184165136400665L;

    public CommandException() {
        super();
    }

    public CommandException(Throwable cause) {
        super(cause);
    }
}
