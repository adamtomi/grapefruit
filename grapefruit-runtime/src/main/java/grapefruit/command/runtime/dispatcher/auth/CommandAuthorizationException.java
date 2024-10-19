package grapefruit.command.runtime.dispatcher.auth;

import grapefruit.command.runtime.CommandException;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

/**
 * This excepion is thrown if the user lacks sufficient permissions
 * to execute a command.
 */
public class CommandAuthorizationException extends CommandException {
    @Serial
    private static final long serialVersionUID = -5370583788815358151L;
    private final String permission;

    public CommandAuthorizationException(String permission) {
        this.permission = requireNonNull(permission);
    }

    /**
     * Returns the permission that is required to execute the
     * given command.
     *
     * @return The required permission
     */
    public String permission() {
        return this.permission;
    }
}
