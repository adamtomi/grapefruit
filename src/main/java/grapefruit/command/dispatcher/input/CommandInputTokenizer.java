package grapefruit.command.dispatcher.input;

import org.jetbrains.annotations.Nullable;

public interface CommandInputTokenizer {
    /* Both of these fields are used by StringReader#readQuotable */
    char SINGLE_QUOTE = '\'';
    char DOUBLE_QUOTE = '"';

    // Return the original user input
    String unwrap();

    int cursor();

    void moveTo(final int position);

    boolean hasNext();

    char next() throws MissingInputException;

    char peek();

    void advance() throws MissingInputException;

    @Nullable String peekWord();

    String readWord() throws MissingInputException;

    String readQuotable() throws MissingInputException;

    String readRemaining() throws MissingInputException;

    String consumed();

    String remainingOrEmpty();

    static CommandInputTokenizer wrap(final String input) {
        return new CommandInputTokenizerImpl(input);
    }
}
