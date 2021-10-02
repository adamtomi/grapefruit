package grapefruit.command.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandContextTests {

    @Test
    public void source_equality() {
        final Object source = new Object();
        final CommandContext<Object> context = new CommandContext<>(source, "", Map.of());
        assertEquals(context.source(), source);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello there", "root child", "a b c"})
    public void commandLine_equality(final String commandLine) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), commandLine, Map.of());
        assertEquals(context.commandLine(), commandLine);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void putDefault_resultIsEmpty(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of());
        context.putDefault(name, new Object());
        assertTrue(context.find(name).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void find_validInput(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of());
        context.putDefault(name, "a");
        context.put(name, new Object());
        assertTrue(context.find(name).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findUnchecked_validInput_doesNotThrow(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of());
        context.put(name, new Object());
        assertDoesNotThrow(() -> context.findUnchecked(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void find_nullInput(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of());
        context.put(name, null);
        assertTrue(context.find(name).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findUnchecked_nullInput_throws(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of());
        context.put(name, null);
        assertThrows(NoSuchElementException.class, () -> context.findUnchecked(name));
    }

    @Test
    public void argCount_validInput() {
        final List<String> options = Arrays.asList("first", "some-name", "some-other-name", "this-is-my-name");
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of());
        options.forEach(each -> context.put(each, new Object()));
        assertEquals(options.size(), context.argCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findAt_validInput(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, name));
        context.put(name, "");
        assertTrue(context.findAt(0).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findAt_validInput_doesNotThrow(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, name));
        context.put(name, "");
        assertDoesNotThrow(() -> context.findAtUnchecked(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findAt_nullInput(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, name));
        context.put(name, null);
        assertTrue(context.findAt(0).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findAt_nullInput_throws(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, name));
        context.put(name,  null);
        assertThrows(NoSuchElementException.class, () -> context.findAtUnchecked(0));
    }
}
