package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandContext;

import java.io.Serial;

public class ArgumentMappingException extends CommandException {
    @Serial
    private static final long serialVersionUID = 5610790418254180598L;

    public ArgumentMappingException() {
        super();
    }

    public interface Factory<T> {

        ArgumentMappingException create(final CommandContext<?> context, final T value);
    }
}
