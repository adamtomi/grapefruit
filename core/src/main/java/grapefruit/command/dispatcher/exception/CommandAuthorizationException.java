package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class CommandAuthorizationException extends CommandException {
    @Serial
    private static final long serialVersionUID = -2958070605704162413L;
    private final String requiredPermission;

    public CommandAuthorizationException(final @NotNull String requiredPermission) {
        super(Message.of(
                MessageKeys.AUTHORIZATION_ERROR,
                Template.of("{permission}", requireNonNull(requiredPermission, "requiredPermission cannot be null"))
        ));
        this.requiredPermission = requiredPermission;
    }

    public @NotNull String requiredPermission() {
        return this.requiredPermission;
    }
}
