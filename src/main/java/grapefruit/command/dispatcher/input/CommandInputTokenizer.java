package grapefruit.command.dispatcher.input;

import org.jetbrains.annotations.Nullable;

public interface CommandInputTokenizer {
    /* Both of these fields are used by StringReader#readQuotable */
    char SINGLE_QUOTE = '\'';
    char DOUBLE_QUOTE = '"';

    boolean hasNext();

    char next() throws CommandSyntaxException;

    String raw();

    @Nullable String peekWord();

    String readWord() throws CommandSyntaxException;

    String readQuotable() throws CommandSyntaxException;

    String readRemaining() throws CommandSyntaxException;

    String consumed();

    String remainingOrEmpty();

    static CommandInputTokenizer wrap(final String input) {
        return new CommandInputTokenizerImpl(input);
    }
}
