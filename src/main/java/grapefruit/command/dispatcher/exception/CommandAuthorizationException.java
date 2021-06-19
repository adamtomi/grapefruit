package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class CommandAuthorizationException extends CommandException {
    @Serial
    private static final long serialVersionUID = -2958070605704162413L;
    private final String requiredPermission;

    public CommandAuthorizationException(final @NotNull String requiredPermission) {
        super();
        this.requiredPermission = requireNonNull(requiredPermission, "requiredPermission cannot be null");
    }

    public @NotNull String requiredPermission() {
        return this.requiredPermission;
    }
}
