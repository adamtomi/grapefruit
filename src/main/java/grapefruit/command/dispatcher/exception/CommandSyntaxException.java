package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandSyntax;
import grapefruit.command.message.Message;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class CommandSyntaxException extends CommandException {
    private final String rawSyntax;
    private final List<String> syntaxOptions;
    @Serial
    private static final long serialVersionUID = 8701865850834783428L;

    public CommandSyntaxException(final @NotNull String rawSyntax,
                                  final @NotNull List<String> syntaxOptions,
                                  final @NotNull Message message) {
        super(null, message);
        this.rawSyntax = requireNonNull(rawSyntax, "rawSyntax cannot be null");
        this.syntaxOptions = requireNonNull(syntaxOptions, "syntaxOptions cannot be null");
    }

    public CommandSyntaxException(final @NotNull CommandSyntax syntax,
                                  final @NotNull Message message) {
        this(syntax.rawSyntax(), syntax.syntaxOptions(), message);
    }

    public @NotNull String rawSyntax() {
        return this.rawSyntax;
    }

    public @NotNull List<String> syntaxOptions() {
        return List.copyOf(this.syntaxOptions);
    }
}
