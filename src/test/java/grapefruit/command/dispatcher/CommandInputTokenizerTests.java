package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.CommandSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandInputTokenizerTests {

    @Test
    public void peek() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(" ab "); // ' '
        assertEquals(' ', input.peek());
        assertDoesNotThrow(input::advance); // -> a
        assertEquals('a', input.peek());
        assertDoesNotThrow(input::advance); // -> b
        assertEquals('b', input.peek());
        assertDoesNotThrow(input::advance); // -> ' '
        assertEquals(' ', input.peek());
        assertDoesNotThrow(input::advance); // -> Mark the input consumed
        assertThrows(CommandSyntaxException.class, input::advance);
    }

    @Test
    public void unwrap() {
        final String arg = " this is a command input";
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(arg);
        assertEquals(arg, input.unwrap());
    }

    @ParameterizedTest
    @ValueSource(strings = { " hello ", "value ", " world" })
    public void peekWord_nonNull(final String arg) {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(arg);
        assertEquals(arg.trim(), input.peekWord());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "      "
    })
    public void peekWord_null(final String arg) {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(arg);
        assertNull(input.peekWord());
    }

    @ParameterizedTest
    @ValueSource(strings = { " hello ", "value ", " world" })
    public void readWord_doesNotThrow(final String arg) {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(arg);
        assertDoesNotThrow(() -> assertEquals(arg.trim(), input.readWord()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "      "
    })
    public void readWord_doesThrow(final String arg) {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(arg);
        assertThrows(CommandSyntaxException.class, input::readWord);
    }

    @ParameterizedTest
    @ValueSource(strings = { " hello ", "value ", " world" })
    public void readQuotable_notQuoted(final String arg) {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(arg);
        assertDoesNotThrow(() -> assertEquals(arg.trim(), input.readQuotable()));
    }

    @ParameterizedTest
    @CsvSource({
            "\" hello\",' hello'",
            "' world ', world ",
            "\"hello world \",'hello world '"
    })
    public void readQuotable_quoted(final String quoted, final String unquoted) {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(quoted);
        assertDoesNotThrow(() -> assertEquals(unquoted, input.readQuotable()));
    }

    @ParameterizedTest
    @CsvSource({
            "hello this is a long message,this is a long message",
            "'another message ','message '"
    })
    public void readRemaining_doesNotThrow(final String first, final String second) {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(first);
        assertDoesNotThrow(input::readWord);
        assertDoesNotThrow(() -> assertEquals(second, input.readRemaining()));
    }

    @Test
    public void readRemaining_doesThrow_empty() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap("");
        assertThrows(CommandSyntaxException.class, input::readRemaining);
    }
}
