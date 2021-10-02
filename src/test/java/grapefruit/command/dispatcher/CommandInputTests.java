package grapefruit.command.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandInputTests {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -144, -45})
    public void blankCommandInput_invalidLength(final int length) {
        assertThrows(IllegalArgumentException.class, () -> new BlankCommandInput(length));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 13, 154})
    public void blankCommandInput_validLength(final int length) {
        final BlankCommandInput input = new BlankCommandInput(length);
        assertEquals(" ".repeat(length), input.rawArg());
    }

    @Test
    public void blankCommandInput_notConsumed_isConsumed() {
        final BlankCommandInput input = new BlankCommandInput(1);
        assertFalse(input.isConsumed());
    }

    @Test
    public void blankCommandInput_markedConsumed_isConsumed() {
        final BlankCommandInput input = new BlankCommandInput(1);
        input.markConsumed();
        assertTrue(input.isConsumed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "-aa", " a bb a"})
    public void stringCommandInput_rawArg(final String rawArg) {
        final StringCommandInput input = new StringCommandInput(rawArg);
        assertEquals(rawArg, input.rawArg());
    }

    @Test
    public void stringCommandInput_notConsumed_isConsumed() {
        final StringCommandInput input = new StringCommandInput("");
        assertFalse(input.isConsumed());
    }

    @Test
    public void stringCommandInput_markedConsumed_isConsumed() {
        final StringCommandInput input = new StringCommandInput("");
        input.markConsumed();
        assertTrue(input.isConsumed());
    }

    @Test
    public void tokenizer_tokenize_simpleInput() {
        final String commandLine = "root sub 10 a";
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> expected = new ConcurrentLinkedQueue<>(Arrays.asList(
                new StringCommandInput("root"),
                new StringCommandInput("sub"),
                new StringCommandInput("10"),
                new StringCommandInput("a")
        ));
        assertTrue(contentEquals(expected, tokenizer.tokenizeInput(commandLine)));
    }

    @Test
    public void tokenizer_tokenize_inputWithWhitespaces() {
        final String commandLine = " root sub  aaa b ccc 203  a45";
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> expected = new ConcurrentLinkedQueue<>(Arrays.asList(
                new StringCommandInput("root"),
                new StringCommandInput("sub"),
                new StringCommandInput("aaa"),
                new StringCommandInput("b"),
                new StringCommandInput("ccc"),
                new StringCommandInput("203"),
                new StringCommandInput("a45")
        ));
        assertTrue(contentEquals(expected, tokenizer.tokenizeInput(commandLine)));
    }

    @Test
    public void tokenizer_tokenize_stringAndBlankInput() {
        final String commandLine = "root sub aaa    c   sd  ab4 ";
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> expected = new ConcurrentLinkedQueue<>(Arrays.asList(
                new StringCommandInput("root"),
                new StringCommandInput("sub"),
                new StringCommandInput("aaa"),
                new BlankCommandInput(2),
                new StringCommandInput("c"),
                new BlankCommandInput(1),
                new StringCommandInput("sd"),
                new StringCommandInput("ab4")
        ));
        assertTrue(contentEquals(expected, tokenizer.tokenizeInput(commandLine)));
    }

    private static boolean contentEquals(final Queue<CommandInput> expected,
                                         final Queue<CommandInput> result) {
        for (final CommandInput input : expected) {
            if (!result.contains(input)) {
                return false;
            }
        }

        return true;
    }
}
