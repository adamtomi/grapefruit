package grapefruit.command.dispatcher.input;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.function.Function3;

import java.util.Optional;

public interface CommandInputTokenizer {

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

    Optional<String> lastConsumed();

    String remaining();

    Internal internal();

    static CommandInputTokenizer wrap(final String input) {
        return new CommandInputTokenizerImpl(input);
    }

    interface Internal {

        <X extends CommandArgumentException> X gen(final String argument, final Function3<String, String, String, X> provider);
    }
}
