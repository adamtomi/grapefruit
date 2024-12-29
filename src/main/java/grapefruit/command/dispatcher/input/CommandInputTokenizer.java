package grapefruit.command.dispatcher.input;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.function.Function3;

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

    String remaining();

    static CommandInputTokenizer.Internal wrap(final String input) {
        return new CommandInputTokenizerImpl(input);
    }

    interface Internal extends CommandInputTokenizer {

        Unsafe unsafe();
    }

    interface Unsafe {

        Optional<String> lastConsumed();

        <X extends CommandArgumentException> X exception(final String argument, final Function3<String, String, String, X> provider);
    }
}
