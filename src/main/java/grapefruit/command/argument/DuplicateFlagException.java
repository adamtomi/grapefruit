package grapefruit.command.argument;

import grapefruit.command.dispatcher.input.CommandInputTokenizer;

import java.io.Serial;

public class DuplicateFlagException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = 5344480879012741190L;

    private DuplicateFlagException(final String consumed, final String argument, final String remaining) {
        super(consumed, argument, remaining);
    }

    public static DuplicateFlagException fromInput(final CommandInputTokenizer input, final String argument) {
        return new DuplicateFlagException(input.consumed(), argument, input.remainingOrEmpty());
    }
}
