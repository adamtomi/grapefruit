package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.mapper.AbstractParameterMapper;
import grapefruit.command.util.AnnotationList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandContextTests {

    @Test
    public void source_equality() {
        final Object source = new Object();
        final CommandContext<Object> context = new CommandContext<>(source, "", Map.of(), Map.of());
        assertEquals(context.source(), source);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello there", "root child", "a b c"})
    public void commandLine_equality(final String commandLine) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), commandLine, Map.of(), Map.of());
        assertEquals(context.commandLine(), commandLine);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void find_validInput(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(), new HashMap<>());
        context.put(name, new Object());
        assertTrue(context.find(name).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findUnchecked_validInput_doesNotThrow(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(), new HashMap<>());
        context.put(name, new Object());
        assertDoesNotThrow(() -> context.findUnchecked(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void find_nullInput(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(), new HashMap<>());
        context.put(name, null);
        assertTrue(context.find(name).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findUnchecked_nullInput_throws(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(), new HashMap<>());
        context.put(name, null);
        assertThrows(NoSuchElementException.class, () -> context.findUnchecked(name));
    }

    @Test
    public void argCount_validInput() {
        final List<String> options = Arrays.asList("first", "some-name", "some-other-name", "this-is-my-name");
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(), new HashMap<>());
        options.forEach(each -> context.put(each, new Object()));
        assertEquals(options.size(), context.argCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findAt_validInput(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, name), new HashMap<>());
        context.put(name, "");
        assertTrue(context.findAt(0).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findAt_validInput_doesNotThrow(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, name), new HashMap<>());
        context.put(name, "");
        assertDoesNotThrow(() -> context.findAtUnchecked(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findAt_nullInput(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, name), new HashMap<>());
        context.put(name, null);
        assertTrue(context.findAt(0).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third", "_a", " aab"})
    public void findAt_nullInput_throws(final String name) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, name), new HashMap<>());
        context.put(name,  null);
        assertThrows(NoSuchElementException.class, () -> context.findAtUnchecked(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 100, 45, 231})
    public void findAt_invalidIndex(final int index) {
        final CommandContext<Object> context = new CommandContext<>(new Object(), "", Map.of(0, ""), new HashMap<>());
        assertTrue(context.findAt(index).isEmpty());
    }

    @Test
    public void asMap_sizeEquals() {
        final List<CommandParameter<Object>> params = List.of(
                new DummyParameter("first"),
                new DummyParameter("second"),
                new DummyParameter("third")
        );
        final CommandContext<Object> context = CommandContext.create(new Object(), "", params);
        assertEquals(3, context.asMap().size());
    }

    @Test
    public void asMap_contentEquals() {
        final List<CommandParameter<Object>> params = List.of(
                new DummyParameter("first"),
                new DummyParameter("second"),
                new DummyParameter("third")
        );
        final CommandContext<Object> context = CommandContext.create(new Object(), "", params);
        final Map<String, Object> values = new LinkedHashMap<>();
        for (final CommandParameter<Object> param : params) {
            values.put(param.name(), null);
        }

        assertEquals(values, context.asMap());
    }

    private static final class DummyParameter extends StandardParameter<Object> {
        private DummyParameter(final String name) {
            super(name, false, TypeToken.of(Object.class), new AnnotationList(), new DummyParameterMapper());
        }
    }

    private static final class DummyParameterMapper extends AbstractParameterMapper<Object, String> {
        private DummyParameterMapper() {
            super(TypeToken.of(String.class));
        }

        @Override
        public String map(final CommandContext<Object> context,
                          final Queue<CommandInput> args,
                          final AnnotationList modifiers) {
            return "Hello there!";
        }
    }
}
