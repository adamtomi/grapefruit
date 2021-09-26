package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public class DuplicateFlagException extends CommandException {
    @Serial
    private static final long serialVersionUID = -6441200625926694985L;
    private final String flag;

    public DuplicateFlagException(final @NotNull String flag) {
        super(Message.of(MessageKeys.DUPLICATE_FLAG, Template.of("{flag}", flag)));
        this.flag = flag;
    }

    public @NotNull String flag() {
        return this.flag;
    }
}
