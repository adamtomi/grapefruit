package grapefruit.command.argument;

import grapefruit.command.argument.mapper.ArgumentMappingException;
import grapefruit.command.argument.mapper.builtin.EnumArgumentMapper;
import grapefruit.command.mock.NilCommandContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static grapefruit.command.argument.mapper.builtin.NumericArgumentMapper.floatMapper;
import static grapefruit.command.argument.mapper.builtin.NumericArgumentMapper.intMapper;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.greedy;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.quotable;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.regex;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.word;
import static grapefruit.command.testutil.Helper.inputOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgumentMapperTests {

    @Test
    public void stringArgumentMapper_word() {
        final String word = "hello";
        assertDoesNotThrow(() -> assertEquals(word, word().tryMap(new NilCommandContext(), inputOf(word))));
    }

    @Test
    public void stringArgumentMapper_quotable() {
        final String content = "hello ";
        final String command = "\"%s\" world".formatted(content);
        assertDoesNotThrow(() -> assertEquals(content, quotable().tryMap(new NilCommandContext(), inputOf(command))));
    }

    @Test
    public void stringArgumentMapper_greedy() {
        final String command = "hello world";
        assertDoesNotThrow(() -> assertEquals(command, greedy().tryMap(new NilCommandContext(), inputOf(command))));
    }

    // @Test // TODO fix
    public void stringArgumentMapper_regex_doesNotMatch() {
        assertThrows(
                ArgumentMappingException.class,
                () -> word().with(regex(Pattern.compile("[a-z]+"))).tryMap(new NilCommandContext(), inputOf("$hello$"))
        );
    }

    @Test
    public void stringArgumentMapper_regex_doesMatch() {
        assertDoesNotThrow(
                () -> word().with(regex(Pattern.compile("[a-z]+"))).tryMap(new NilCommandContext(), inputOf("hello"))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "''",
            "abc",
            "$",
            "11.44",
            "--344",
            "2147483648" // 2^32 (Integer#MAX_VALUE + 1)
    })
    public void numericArgumentMapper_int_invalidInput(final String arg) {
        assertThrows(ArgumentMappingException.class, () -> intMapper().tryMap(new NilCommandContext(), inputOf(arg)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "44",
            "-44",
            "3.14",
            "-3.14"
    })
    public void numericArgumentMapper_float_validInput(final String arg) {
        assertDoesNotThrow(() -> floatMapper().tryMap(new NilCommandContext(), inputOf(arg)));
    }

    @Test
    public void enumArgumentMapper_strict_tryMap() {
        final EnumArgumentMapper<Object, TimeUnit> mapper = EnumArgumentMapper.strict(TimeUnit.class);
        assertThrows(ArgumentMappingException.class, () -> mapper.tryMap(new NilCommandContext(), inputOf("seconds")));
        assertDoesNotThrow(() -> assertEquals(TimeUnit.SECONDS, mapper.tryMap(new NilCommandContext(), inputOf("SECONDS"))));
    }

    @Test
    public void enumArgumentMapper_lenient_tryMap() {
        final EnumArgumentMapper<Object, TimeUnit> mapper = EnumArgumentMapper.lenient(TimeUnit.class);
        assertDoesNotThrow(() -> assertEquals(TimeUnit.SECONDS, mapper.tryMap(new NilCommandContext(), inputOf("seconds"))));
        assertDoesNotThrow(() -> assertEquals(TimeUnit.SECONDS, mapper.tryMap(new NilCommandContext(), inputOf("SECONDS"))));
    }
}
