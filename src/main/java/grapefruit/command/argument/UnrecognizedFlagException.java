package grapefruit.command.argument;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class UnrecognizedFlagException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = -3923231453409088302L;
    private final String exactFlag;

    public UnrecognizedFlagException(final String consumed, final String argument, final String remaining, final String exactFlag) {
        super(consumed, argument, remaining);
        this.exactFlag = requireNonNull(exactFlag, "exactFlag");
    }

    public UnrecognizedFlagException(final String consumed, final String argument, final String remaining) {
        this(consumed, argument, remaining, argument);
    }

    public String exactFlag() {
        return this.exactFlag;
    }
}
