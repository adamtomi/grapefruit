package grapefruit.command.parameter.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.parameter.CommandParameter0;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class ParameterMappingException extends CommandException {
    @Serial
    private static final long serialVersionUID = -1767767263253587926L;

    public ParameterMappingException(final @NotNull Message message) {
        super(requireNonNull(message, "message cannot be null"));
    }

    public @NotNull CommandParameter0 parameter() {
        throw new UnsupportedOperationException();
    }
}
