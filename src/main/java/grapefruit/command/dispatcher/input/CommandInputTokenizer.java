package grapefruit.command.dispatcher.input;

import org.jetbrains.annotations.Nullable;

public interface CommandInputTokenizer {
    /* Both of these fields are used by StringReader#readQuotable */
    char SINGLE_QUOTE = '\'';
    char DOUBLE_QUOTE = '"';

    // Return the original user input
    String unwrap();

    boolean hasNext();

    char next() throws CommandSyntaxException;

    char peek();

    void advance() throws CommandSyntaxException;

    @Deprecated
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
