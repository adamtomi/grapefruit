package grapefruit.command.parameter.mapper;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.dispatcher.CommandInputTokenizer;
import grapefruit.command.parameter.mapper.builtin.StringMapper;
import grapefruit.command.parameter.modifier.string.Greedy;
import grapefruit.command.parameter.modifier.string.Quotable;
import grapefruit.command.parameter.modifier.string.Regex;
import grapefruit.command.util.AnnotationList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StringMapperTests {
    private Quotable quotableAnnotation;
    private Greedy greedyAnnotation;

    @BeforeAll
    public void setUp() throws Exception {
        final Method method = getClass().getDeclaredMethod("dummyMethod", String.class, String.class);
        final Parameter[] params = method.getParameters();
        this.quotableAnnotation = params[0].getAnnotation(Quotable.class);
        this.greedyAnnotation = params[1].getAnnotation(Greedy.class);
    }

    private void dummyMethod(final @Quotable String quotable, final @Greedy String greedy) {}

    private static CommandContext<Object> dummyCommandContext() {
        return CommandContext.create(new Object(), "", List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"input", "other-input", "string", "hi", " ..", "-_-_-_-"})
    public void map_simpleInput(final String input) throws ParameterMappingException {
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertEquals(input.trim(), stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @Test
    public void map_quotable_missingTrailingQuote() {
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput("\"first second third");
        assertThrows(ParameterMappingException.class, () ->
                stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(this.quotableAnnotation)));
    }

    @ParameterizedTest
    @CsvSource({
            "input,input",
            "some input,some",
            "some other input,some",
            "\"my input\",my input",
            "\" this is an input as well  with   white spaces\",' this is an input as well with   white spaces'",
            "\"    \",'    '"
    })
    public void map_quotable_validInput(final String input, final String expected) throws ParameterMappingException {
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        final String result = stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(this.quotableAnnotation));
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "hello there",
            "this is a greedy string",
            " this string is even more greedy!"
    })
    public void map_greedy_queueIsEmpty(final String input) throws ParameterMappingException {
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(this.greedyAnnotation));
        assertTrue(inputQueue.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "hello there,hello there",
            "this is a greedy string,this is a greedy string",
            " this string is even more greedy!,this string is even more greedy!",
            "'  hello there  again, I'm here  for ya   .',' hello there again, I'm here for ya   .'"
    })
    public void map_greedy_inputEquals(final String input, final String expected) throws ParameterMappingException {
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        final String result = stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(this.greedyAnnotation));
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "hi-there",
            "th1s-string-should-match",
            "An0ther_str1ng_that_m4tch3s"
    })
    public void map_simpleRegex_matches(final String input) {
        final Regex regex = new RegexImpl("[0-9a-zA-Z_-]+");
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertDoesNotThrow(() -> stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(regex)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "hi there",
            "th1s-string-shouldn't-match",
            "Another str1ng_ ",
            "__  ...",
            " ",
            "\n",
            "áááá"
    })
    public void map_simpleRegex_doesNotMatch(final String input) {
        final Regex regex = new RegexImpl("[0-9a-z]+");
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertThrows(ParameterMappingException.class,
                () -> stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(this.greedyAnnotation, regex)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "hi-there",
            "háháhá",
            "hihih\u00ed",
            "áááá",
            "\u00C9\u00C9\u00C9"
    })
    public void map_regexAllowUnicode_matches(final String input) {
        final Regex regex = new RegexImpl("(\\w|_|-)+", Pattern.UNICODE_CHARACTER_CLASS);
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertDoesNotThrow(() ->
                stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(this.greedyAnnotation, regex)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Hello",
            "aBcD",
            "09bNcH"
    })
    public void map_regexCaseInsensitive_matches(final String input) {
        final Regex regex = new RegexImpl("[0-9a-z]+", Pattern.CASE_INSENSITIVE);
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertDoesNotThrow(() ->
                stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(this.greedyAnnotation, regex)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Hello",
            "aBcD",
            "09bNcH",
            "\u00C9\u00C9\u00C9",
            "hihih\u00ed",
    })
    public void map_regexCaseInsensitive_allowUnicode_matches(final String input) {
        final Regex regex = new RegexImpl("[0-9|\\w]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertDoesNotThrow(() ->
                stringMapper.map(dummyCommandContext(), inputQueue, new AnnotationList(this.greedyAnnotation, regex)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"input", "other-input", "string", "hi", " ..", "-_-_-_-"})
    public void listSuggestions_empty(final String input) {
        final StringMapper<Object> stringMapper = new StringMapper<>();
        final List<String> suggestions = stringMapper.listSuggestions(dummyCommandContext(), input, new AnnotationList());
        assertTrue(suggestions.isEmpty());
    }

    @SuppressWarnings("all")
    private static final class RegexImpl implements Regex, Serializable {
        @Serial
        private static final long serialVersionUID = -1013588770689577267L;
        private final String value;
        private final int flags;

        private RegexImpl(final String value,
                          final int flags) {
            this.value = value;
            this.flags = flags;
        }

        private RegexImpl(final String value) {
            this(value, 0);
        }

        @Override
        public String value() {
            return this.value;
        }

        @Override
        public boolean allowUnicode() {
            return (this.flags & Pattern.UNICODE_CHARACTER_CLASS) != 0;
        }

        @Override
        public boolean caseInsensitive() {
            return (this.flags & Pattern.CASE_INSENSITIVE) != 0;
        }

        @Override
        public int flags() {
            return this.flags;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Regex.class;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Regex regex)) {
                return false;
            }

            return regex.value().equals(this.value) && regex.flags() == this.flags;
        }

        @Override
        public int hashCode() {
            int result = 0;
            int PRIME = 127;
            result += (PRIME * "value".hashCode()) ^ value().hashCode();
            result += (PRIME * "allowUnicode".hashCode()) ^ Boolean.hashCode(allowUnicode());
            result += (PRIME * "caseInsensitive".hashCode()) ^ Boolean.hashCode(caseInsensitive());
            result += (PRIME * "flags".hashCode()) ^ flags();
            return result;
        }

        @Override
        public String toString() {
            return Regex.class.getName() + "(" +
                    "flags=" + this.flags + ", " +
                    "value=\"" + this.value + "\")";
        }
    }
}
