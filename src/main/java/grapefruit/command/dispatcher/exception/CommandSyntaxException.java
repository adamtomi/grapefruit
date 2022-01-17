package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.message.Message;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class CommandSyntaxException extends CommandException {
    private final String rawSyntax;
    @Serial
    private static final long serialVersionUID = 8701865850834783428L;

    public CommandSyntaxException(final @NotNull String rawSyntax,
                                  final @NotNull Message message) {
        super(null, message);
        this.rawSyntax = requireNonNull(rawSyntax, "rawSyntax cannot be null");
    }

    public @NotNull String rawSyntax() {
        return this.rawSyntax;
    }
}
