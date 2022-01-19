package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandSyntax;
import grapefruit.command.message.Message;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class CommandSyntaxException extends CommandException {
    private final CommandSyntax syntax;
    @Serial
    private static final long serialVersionUID = 8701865850834783428L;

    public CommandSyntaxException(final @NotNull CommandSyntax syntax,
                                  final @NotNull Message message) {
        super(null, message);
        this.syntax = requireNonNull(syntax, "syntax cannot be null");
    }

    public @NotNull CommandSyntax generatedSyntax() {
        return this.syntax;
    }
}
