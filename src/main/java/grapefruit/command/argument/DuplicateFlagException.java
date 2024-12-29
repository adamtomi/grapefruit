package grapefruit.command.argument;

import java.io.Serial;

public class DuplicateFlagException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = 5344480879012741190L;

    public DuplicateFlagException(final String consumed, final String argument, final String remaining) {
        super(consumed, argument, remaining);
    }
}
