package grapefruit.command.dispatcher.input;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.function.Function3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface CommandInputTokenizer {

    // Return the original user input
    String input();

    int cursor();

    int length();

    boolean canRead();

    boolean canReadNonWhitespace();

    char read() throws MissingInputException;

    char peek();

    String peekWord();

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

        Optional<String> lastConsumed();

        <X extends CommandArgumentException> X exception(final String argument, final Function3<String, String, String, X> provider);
    }
}
