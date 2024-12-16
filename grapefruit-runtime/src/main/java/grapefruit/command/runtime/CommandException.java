package grapefruit.command.runtime;

import java.io.Serial;

/**
 * A general command exception signalling some error during
 * the tokenization, parsing, or execution of a command. More
 * specific implementations may provide more details as to
 * what went wrong.
 */
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
