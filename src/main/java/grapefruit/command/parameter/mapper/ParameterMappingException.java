package grapefruit.command.parameter.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class ParameterMappingException extends CommandException {
    @Serial
    private static final long serialVersionUID = -1767767263253587926L;
    private final CommandParameter parameter;

    public ParameterMappingException(final @NotNull Message message, final @NotNull CommandParameter parameter) {
        super(requireNonNull(message, "message cannot be null"));
        this.parameter = requireNonNull(parameter, "parameter cannot be null");
    }

    public @NotNull CommandParameter parameter() {
        return this.parameter;
    }
}
