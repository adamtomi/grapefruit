package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class IllegalCommandSourceException extends CommandException {
    @Serial
    private static final long serialVersionUID = 5820427891800098353L;
    private final Class<?> requiredCommandSourceType;
    private final Class<?> commandSourceType;

    public IllegalCommandSourceException(final @NotNull Class<?> requiredCommandSourceType,
                                         final @NotNull Class<?> commandSourceType) {
        super(Message.of(
                MessageKeys.ILLEGAL_COMMAND_SOURCE,
                Template.of("{required}",
                        requireNonNull(requiredCommandSourceType, "requiredCommandSourceType cannot be null").getName()),
                Template.of("{found}",
                        requireNonNull(commandSourceType, "commandSourceType cannot be null").getName())
        ));
        this.requiredCommandSourceType = requiredCommandSourceType;
        this.commandSourceType = commandSourceType;
    }

    public @NotNull Class<?> requiredCommandSourceType() {
        return this.requiredCommandSourceType;
    }

    public @NotNull Class<?> foundCommandSourceType() {
        return this.commandSourceType;
    }
}
