package grapefruit.command.dispatcher.input;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.function.Function3;
import org.jetbrains.annotations.Nullable;

public interface CommandInputTokenizer {
    /* Both of these fields are used by StringReader#readQuotable */
    char SINGLE_QUOTE = '\'';
    char DOUBLE_QUOTE = '"';

    // Return the original user input
    String unwrap();

    int cursor();

    boolean canRead();

    char read() throws MissingInputException;

    char peek();

    @Nullable String peekWord();

    String readWord() throws MissingInputException;

    String readQuotable() throws MissingInputException;

    String readRemaining() throws MissingInputException;

    String consumed();

    // TODO figure out if we really don't want this
    @Deprecated
    String remainingOrEmpty();

    // TODO figure out if we really need this
    @Nullable String peekRemaining();

    static CommandInputTokenizer wrap(final String input) {
        return new CommandInputTokenizerImpl(input);
    }

    interface Internal extends CommandInputTokenizer {

        Unsafe unsafe();
    }

    interface Unsafe {

        void moveTo(final int position);

        String lastConsumed();

        <X extends CommandArgumentException> X exception(final String argument, final Function3<String, String, String, X> provider);
    }
}
