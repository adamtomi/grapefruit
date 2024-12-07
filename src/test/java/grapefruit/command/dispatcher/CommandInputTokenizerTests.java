package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.CommandSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandInputTokenizerTests {

    @Test
    public void hasNext_noWhitespace() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap("abc"); // a
        assertTrue(input.hasNext());
        assertDoesNotThrow(input::advance); // -> b

        assertTrue(input.hasNext());
        assertDoesNotThrow(input::advance); // -> c

        assertFalse(input.hasNext());
        assertThrows(CommandSyntaxException.class, input::advance);
    }

    @Test
    public void hasNext_whitespace() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(" a "); // ' '
        assertTrue(input.hasNext());
        assertDoesNotThrow(input::advance); // -> a

        assertTrue(input.hasNext());
        assertDoesNotThrow(input::advance); // -> ' '

        assertFalse(input.hasNext());
        assertThrows(CommandSyntaxException.class, input::next);
    }

    @Test
    public void next() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(" abc def "); // ' '
        assertDoesNotThrow(() -> assertEquals('a', input.next())); // -> a
        assertDoesNotThrow(() -> assertEquals('b', input.next())); // -> b
        assertDoesNotThrow(() -> assertEquals('c', input.next())); // -> c
        assertDoesNotThrow(() -> assertEquals(' ', input.next())); // -> ' '
        assertDoesNotThrow(() -> assertEquals('d', input.next())); // -> d
        assertDoesNotThrow(() -> assertEquals('e', input.next())); // -> e
        assertDoesNotThrow(() -> assertEquals('f', input.next())); // -> f
        assertDoesNotThrow(() -> assertEquals(' ', input.next())); // -> ' '
        assertThrows(CommandSyntaxException.class, input::next);
    }

    @Test
    public void peek() {
        final CommandInputTokenizer input = CommandInputTokenizer.wrap(" ab ");
        assertEquals(' ', input.peek());
        assertDoesNotThrow(input::advance);
        assertEquals('a', input.peek());
        assertDoesNotThrow(input::advance);
        assertEquals('b', input.peek());
        assertDoesNotThrow(input::advance);
        assertEquals(' ', input.peek());
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
}
