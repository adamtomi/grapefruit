package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKey;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class NoSuchCommandException extends CommandException {
    @Serial
    private static final long serialVersionUID = 514631540629935937L;
    private final String rootCommand;
    private final String commandLine;

    public NoSuchCommandException(final @NotNull String rootCommand,
                                  final @NotNull String commandLine) {
        super(Message.of(
                MessageKeys.NO_SUCH_COMMAND,
                Template.of("{name}", requireNonNull(rootCommand, "rootCommand cannot be null"))
        ));
        this.rootCommand = rootCommand;
        this.commandLine = requireNonNull(commandLine, "commandLine cannot be null");
    }

    public @NotNull String rootCommand() {
        return this.rootCommand;
    }

    public @NotNull String commandLine() {
        return this.commandLine;
    }
}
