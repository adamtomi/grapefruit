package grapefruit.command.parameter.mapper;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.dispatcher.CommandInputTokenizer;
import grapefruit.command.parameter.mapper.builtin.BooleanMapper;
import grapefruit.command.util.AnnotationList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BooleanMapperTests {

    private static CommandContext<Object> dummyCommandContext() {
        return CommandContext.create(new Object(), "", List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "true", "yes", "allow"})
    public void map_truthyInput(final String input) throws ParameterMappingException {
        final BooleanMapper<Object> booleanMapper = new BooleanMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertTrue(booleanMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "false", "no", "deny"})
    public void map_falsyInput(final String input) throws ParameterMappingException {
        final BooleanMapper<Object> booleanMapper = new BooleanMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertFalse(booleanMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "3", "a", "input", "nein", "ja"})
    public void map_invalidInput(final String input) {
        final BooleanMapper<Object> booleanMapper = new BooleanMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertThrows(ParameterMappingException.class, () -> booleanMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @Test
    public void listSuggestions_notEmpty() {
        final BooleanMapper<Object> booleanMapper = new BooleanMapper<>();
        final List<String> suggestions = booleanMapper.listSuggestions(dummyCommandContext(), "input", new AnnotationList());
        assertFalse(suggestions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "t", "yes", "deny"})
    public void listSuggestions_contains(final String input) {
        final BooleanMapper<Object> booleanMapper = new BooleanMapper<>();
        final List<String> suggestions = booleanMapper.listSuggestions(dummyCommandContext(), "input", new AnnotationList());
        assertTrue(suggestions.contains(input));
    }
}
