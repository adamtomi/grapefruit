package grapefruit.command.parameter.mapper;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.dispatcher.CommandInputTokenizer;
import grapefruit.command.parameter.mapper.builtin.CharacterMapper;
import grapefruit.command.util.AnnotationList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CharacterMapperTests {

    private static CommandContext<Object> dummyCommandContext() {
        return CommandContext.create(new Object(), "", List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c", "d", "_"})
    public void map_validInput(final String input) {
        final CharacterMapper<Object> characterMapper = new CharacterMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertDoesNotThrow(() -> characterMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c", "d", "_"})
    public void map_validInput_equals(final String input) throws ParameterMappingException {
        final CharacterMapper<Object> characterMapper = new CharacterMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertEquals(input.charAt(0), characterMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"aa", "bbb", "cc ", " dddd"})
    public void map_invalidInput(final String input) {
        final CharacterMapper<Object> characterMapper = new CharacterMapper<>();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        assertThrows(ParameterMappingException.class, () -> characterMapper.map(dummyCommandContext(), inputQueue, new AnnotationList()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c", "d", "_"})
    public void listSuggestions_isEmpty(final String input) {
        final CharacterMapper<Object> characterMapper = new CharacterMapper<>();
        final List<String> suggestions = characterMapper.listSuggestions(dummyCommandContext(), input, new AnnotationList());
        assertTrue(suggestions.isEmpty());
    }
}
