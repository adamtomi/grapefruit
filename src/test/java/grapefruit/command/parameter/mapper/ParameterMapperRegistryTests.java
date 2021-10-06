package grapefruit.command.parameter.mapper;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Queue;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParameterMapperRegistryTests {

    @ParameterizedTest
    @ValueSource(classes = {BigDecimal.class, BigInteger.class, Object.class, Number.class, Logger.class})
    public void findMapper_shouldNotFind(final Class<?> clazz) {
        final TypeToken<?> type = TypeToken.of(clazz);
        final ParameterMapperRegistry<?> registry = new ParameterMapperRegistry<>();
        assertTrue(registry.findMapper(type).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "another name", "this is a name too"})
    public void findNamedMapper_shouldNotFind(final String name) {
        final ParameterMapperRegistry<?> registry = new ParameterMapperRegistry<>();
        assertTrue(registry.findNamedMapper(name).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(classes = {BigDecimal.class, BigInteger.class, Object.class, Number.class, Logger.class})
    public void registerMapper_find_shouldFind(final Class<?> clazz) {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final TypeToken<?> type = TypeToken.of(clazz);
        final DummyParameterMapper<?> mapper = new DummyParameterMapper<>(type);
        registry.registerMapper(mapper);
        assertTrue(registry.findMapper(type).isPresent());
    }

    @ParameterizedTest
    @ValueSource(classes = {BigDecimal.class, BigInteger.class, Object.class, Number.class, Logger.class})
    public void registerMapper_find_equals(final Class<?> clazz) {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final TypeToken<?> type = TypeToken.of(clazz);
        final DummyParameterMapper<?> mapper = new DummyParameterMapper<>(type);
        registry.registerMapper(mapper);
        assertEquals(mapper, registry.findMapper(type).orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "another name", "this is a name too"})
    public void registerNamedMapper_find_shouldFind(final String name) {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final TypeToken<?> type = TypeToken.of(Object.class);
        final DummyParameterMapper<?> mapper = new DummyParameterMapper<>(type);
        registry.registerNamedMapper(name, mapper);
        assertTrue(registry.findNamedMapper(name).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "another name", "this is a name too"})
    public void registerNamedMapper_find_equals(final String name) {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final TypeToken<?> type = TypeToken.of(Object.class);
        final DummyParameterMapper<?> mapper = new DummyParameterMapper<>(type);
        registry.registerNamedMapper(name, mapper);
        assertEquals(mapper, registry.findNamedMapper(name).orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(classes = {BigDecimal.class, BigInteger.class, Object.class, Number.class, Logger.class})
    public void registerMapper_typeAlreadyRegistered(final Class<?> clazz) {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final TypeToken<?> type = TypeToken.of(clazz);
        final DummyParameterMapper<?> mapper = new DummyParameterMapper<>(type);
        registry.registerMapper(mapper);
        assertThrows(IllegalStateException.class, () -> registry.registerMapper(mapper));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "another name", "this is a name too"})
    public void registerNamedMapper_nameAlreadyRegistered(final String name) {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final TypeToken<?> type = TypeToken.of(Object.class);
        final DummyParameterMapper<?> mapper = new DummyParameterMapper<>(type);
        registry.registerNamedMapper(name, mapper);
        assertThrows(IllegalStateException.class, () -> registry.registerNamedMapper(name, mapper));
    }

    private static final class DummyParameterMapper<T> extends AbstractParameterMapper<Object, T> {

        private DummyParameterMapper(final @NotNull TypeToken<T> type) {
            super(type);
        }

        @Override
        public @NotNull T map(final @NotNull CommandContext<Object> context,
                              final @NotNull Queue<CommandInput> args,
                              final @NotNull AnnotationList modifiers) {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
