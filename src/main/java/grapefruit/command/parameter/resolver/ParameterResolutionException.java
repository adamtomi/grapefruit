package grapefruit.command.parameter.resolver;

import grapefruit.command.CommandException;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class ParameterResolutionException extends CommandException {
    @Serial
    private static final long serialVersionUID = -1767767263253587926L;

    public ParameterResolutionException(final @NotNull String message) {
        super(requireNonNull(message));
    }
}
