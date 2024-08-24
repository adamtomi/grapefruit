package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public class UnrecognizedFlagException extends CommandException {
    @Serial
    private static final long serialVersionUID = -999358181000113052L;
    private final String input;

    public UnrecognizedFlagException(final @NotNull String input) {
        super(Message.of(MessageKeys.UNRECOGNIZED_COMMAND_FLAG, Template.of("{input}", input)));
        this.input = input;
    }

    public @NotNull String input() {
        return this.input;
    }
}
