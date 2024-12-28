package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static java.lang.String.join;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandInputTokenizerTests {

    @Test
    public void read() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(" ab ");
        assertEquals(' ', input.peek());  // ' '
        assertDoesNotThrow(() -> assertEquals('a', input.read())); // -> a
        assertDoesNotThrow(() -> assertEquals('b', input.read())); // -> b
        assertDoesNotThrow(() -> assertEquals(' ', input.read())); // -> ' '
        assertThrows(MissingInputException.class, input::read);
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
        assertThrows(MissingInputException.class, input::readWord);
    }

    @ParameterizedTest
    @CsvSource({
            "a b,b",
            "c 1,1",
            "-p 3,3"
    })
    public void readWord_singleCharacter(final String arg, final String expected) {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(arg);
        assertDoesNotThrow(input::readWord);
        assertDoesNotThrow(() -> assertEquals(expected, input.readWord()));
    }

    @Test
    public void readWord_consumed() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap("test");
        assertDoesNotThrow(() -> assertEquals("test", input.readWord()));
        assertThrows(MissingInputException.class, input::readWord);
    }

    @Test
    public void readWord_notConsumed() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap("test arg");
        assertDoesNotThrow(() -> assertEquals("test", input.readWord()));
        assertDoesNotThrow(() -> assertEquals("arg", input.readWord()));
    }

    @Test
    public void readWord_words() {
        final String first = "hello";
        final String second = "world";
        final String third = "test";
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(join(" ", Arrays.asList(first, second, third)));
        assertDoesNotThrow(() -> assertEquals("hello", input.readWord()));
        assertDoesNotThrow(() -> assertEquals("world", input.readWord()));
        assertDoesNotThrow(() -> assertEquals("test", input.readWord()));
        assertThrows(MissingInputException.class, input::readWord);
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
        assertThrows(MissingInputException.class, input::readRemaining);
    }
}
