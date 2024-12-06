package grapefruit.command.argument;

import grapefruit.command.dispatcher.input.CommandInputTokenizer;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class UnrecognizedFlagException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = -3923231453409088302L;
    private final String exactFlag;

    private UnrecognizedFlagException(final String consumed, final String argument, final String remaining, final String exactFlag) {
        super(consumed, argument, remaining);
        this.exactFlag = requireNonNull(exactFlag, "exactFlag");
    }

    public static UnrecognizedFlagException fromInput(final CommandInputTokenizer input, final String argument, final String exactFlag) {
        return new UnrecognizedFlagException(input.consumed(), argument, input.remainingOrEmpty(), exactFlag);
    }

    public String exactFlag() {
        return this.exactFlag;
    }
}
