package grapefruit.command.dispatcher.input;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.Range;
import grapefruit.command.util.function.Function3;
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

    @Deprecated // TODO remove from public API
    char next() throws MissingInputException;

    char read() throws MissingInputException;

    char peek();

    @Deprecated // TODO remove from public API
    void advance() throws MissingInputException;

    @Nullable String peekWord();

    String readWord() throws MissingInputException;

    String readQuotable() throws MissingInputException;

    String readRemaining() throws MissingInputException;

    String consumed();

    // TODO figure out if we really don't want this
    @Deprecated
    String remainingOrEmpty();

    // TODO figure out if we really need this
    String peekRemaining();

    static CommandInputTokenizer wrap(final String input) {
        return new CommandInputTokenizerImpl(input);
    }

    interface Internal extends CommandInputTokenizer {

        Range peekConsumed();

        <X extends CommandArgumentException> X exception(final String argument, final Function3<String, String, String, X> provider);
    }
}
