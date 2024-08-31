package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.input.StringReader;

import java.io.Serial;

public class CommandSyntaxException extends CommandException {
    @Serial
    private static final long serialVersionUID = -6615337831318406658L;

    public static CommandSyntaxException from(StringReader reader, Kind kind) {
        throw new UnsupportedOperationException();
    }

    public enum Kind {
        TOO_FEW_ARGUMENTS, TOO_MANY_ARGUMENTS
    }
}
