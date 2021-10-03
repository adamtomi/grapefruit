package grapefruit.command.parameter.mapper;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.dispatcher.CommandInputTokenizer;
import grapefruit.command.parameter.mapper.builtin.NumberMapper;
import grapefruit.command.parameter.modifier.Range;
import grapefruit.command.util.AnnotationList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Queue;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumberMapperTests {
    private final List<String> numbersFrom0to9 = IntStream.range(1, 10)
            .mapToObj(String::valueOf)
            .toList();

    private static CommandContext<Object> dummyCommandContext() {
        return CommandContext.create(new Object(), "", List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "-121", "0", "1647", "1000", "4000982"})
    public void int_map_validInput(final String input) {
        final NumberMapper<Object, Integer> intMapper = new NumberMapper<>(TypeToken.of(Integer.class), Integer::parseInt);
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertDoesNotThrow(() -> intMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "967a", "cc", "10000000000", "-4568937464"})
    public void short_map_invalidInput(final String input) {
        final NumberMapper<Object, Short> shortMapper = new NumberMapper<>(TypeToken.of(Short.class), Short::parseShort);
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertThrows(ParameterMappingException.class, () -> shortMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "100", "1465392", "314"})
    public void long_map_range_validInput(final String input) {
        final Range range = new RangeImpl(-2.0D, 100000000.0D);
        final NumberMapper<Object, Long> longMapper = new NumberMapper<>(TypeToken.of(Long.class), Long::parseLong);
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertDoesNotThrow(() -> longMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(range)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-101.0F", "510", "692", "-813.45F", "1003.4F"})
    public void float_map_range_invalidInput(final String input) {
        final Range range = new RangeImpl(-100.0D, 450.0D);
        final NumberMapper<Object, Float> floatMapper = new NumberMapper<>(TypeToken.of(Float.class), Float::parseFloat);
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertThrows(ParameterMappingException.class, () -> floatMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(range)));
    }

    @Test
    public void listSuggestions_notEmtpy() {
        final NumberMapper<Object, Byte> byteMapper = new NumberMapper<>(TypeToken.of(Byte.class), Byte::parseByte);
        final List<String> result = byteMapper.listSuggestions(dummyCommandContext(), "127", new AnnotationList());
        assertFalse(result.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-45", "110", "1800", "-9652", "763"})
    public void listSuggestions_dynamicCompletions_validInput(final String input) {
        final NumberMapper<Object, Integer> intMapper = new NumberMapper<>(TypeToken.of(Integer.class), Integer::parseInt);
        final List<String> result = intMapper.listSuggestions(dummyCommandContext(), input, new AnnotationList());
        final List<String> expected = this.numbersFrom0to9.stream()
                .map(x -> input + x)
                .toList();
        assertTrue(result.containsAll(expected));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "23a", "c450", "number", "fifteen"})
    public void listSuggestions_dynamicCompletions_invalidInput(final String input) {
        final NumberMapper<Object, Integer> intMapper = new NumberMapper<>(TypeToken.of(Integer.class), Integer::parseInt);
        final List<String> result = intMapper.listSuggestions(dummyCommandContext(), input, new AnnotationList());
        assertTrue(result.isEmpty());
    }

    @Test
    public void listSuggestions_dynamicCompletions_emptyInput() {
        final NumberMapper<Object, Integer> intMapper = new NumberMapper<>(TypeToken.of(Integer.class), Integer::parseInt);
        final List<String> result = intMapper.listSuggestions(dummyCommandContext(), "", new AnnotationList());
        assertEquals(18, result.size());
    }

    @SuppressWarnings("all")
    private static final class RangeImpl implements Range, Serializable {
        @Serial
        private static final long serialVersionUID = -214491526119439246L;
        private final double min;
        private final double max;

        private RangeImpl(final double min,
                          final double max) {
            if (min > max) {
                throw new IllegalArgumentException("Min cannot be greater than max");
            }

            this.min = min;
            this.max = max;
        }

        @Override
        public double min() {
            return this.min;
        }

        @Override
        public double max() {
            return this.max;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Range.class;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Range range)) {
                return false;
            }

            return this.min == range.min() && this.max == range.max();
        }

        @Override
        public int hashCode() {
            int result = 0;
            final int PRIME = 127;
            result += (PRIME * "min".hashCode()) ^ Double.hashCode(this.min);
            result += (PRIME * "max".hashCode()) ^ Double.hashCode(this.max);
            return result;
        }

        @Override
        public String toString() {
            return Range.class.getName() +
                    "(min=" + this.min + ", " +
                    "max=" + this.max + ')';
        }
    }
}
