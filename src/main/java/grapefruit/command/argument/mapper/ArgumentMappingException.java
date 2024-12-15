package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgumentException;

import java.io.Serial;

public class ArgumentMappingException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = 5610790418254180598L;

    public ArgumentMappingException(final CommandException cause, final String consumed, final String argument, final String remaining) {
        super(cause, consumed, argument, remaining);
    }
}
