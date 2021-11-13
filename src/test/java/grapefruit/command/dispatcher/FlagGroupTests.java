package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.UnrecognizedFlagException;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.parameter.PresenceFlagParameter;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.mapper.AbstractParameterMapper;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.Miscellaneous;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FlagGroupTests {

    @ParameterizedTest
    @ValueSource(strings = {"flag", "other-flag", "another-flag"})
    public void parse_flagNames(final String flag) throws CommandException {
        final List<CommandParameter<Object>> parameters = List.of(
                new PresenceFlagParameter<>(flag, ' ', "arg0", new AnnotationList()),
                new DummyParameter("dummy-0"),
                new DummyParameter("dummy-1")
        );
        final String rawInput = Miscellaneous.formatFlag(flag);
        final Matcher matcher = FlagGroup.VALID_PATTERN.matcher(rawInput);
        final boolean mathches = matcher.matches();
        assert mathches;

        final FlagGroup<Object> group = FlagGroup.parse(rawInput, matcher, parameters);
        int flagCount = 0;
        for (FlagParameter<Object> ignored : group) {
            flagCount++;
        }

        assertEquals(1, flagCount);
    }

    @ParameterizedTest
    @ValueSource(chars = {'a', 'b', 'c', 'd'})
    public void parse_shorthands(final char shorthand) throws CommandException {
        final List<CommandParameter<Object>> parameters = List.of(
                new PresenceFlagParameter<>("flag", shorthand, "arg0", new AnnotationList()),
                new DummyParameter("dummy-0"),
                new DummyParameter("dummy-1")
        );
        final String rawInput = Miscellaneous.formatFlag(String.valueOf(shorthand));
        final Matcher matcher = FlagGroup.VALID_PATTERN.matcher(rawInput);
        final boolean mathches = matcher.matches();
        assert mathches;

        final FlagGroup<Object> group = FlagGroup.parse(rawInput, matcher, parameters);
        int flagCount = 0;
        for (FlagParameter<Object> ignored : group) {
            flagCount++;
        }

        assertEquals(1, flagCount);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "third"})
    public void parse_unrecognizedFlags(final String flagName) {
        final List<CommandParameter<Object>> parameters = List.of(
                new PresenceFlagParameter<>("flag", ' ', "arg0", new AnnotationList()),
                new DummyParameter("dummy-0"),
                new DummyParameter("dummy-1")
        );

        final String rawInput = Miscellaneous.formatFlag(flagName);
        final Matcher matcher = FlagGroup.VALID_PATTERN.matcher(rawInput);
        final boolean mathches = matcher.matches();
        assert mathches;

        assertThrows(UnrecognizedFlagException.class, () -> FlagGroup.parse(rawInput, matcher, parameters));
    }

    @Test
    public void parse_shorthandGroups() throws CommandException {
        final List<CommandParameter<Object>> parameters = List.of(
                new PresenceFlagParameter<>("flag", 'c', "arg0", new AnnotationList()),
                new DummyParameter("dummy-0"),
                new DummyParameter("dummy-1"),
                new PresenceFlagParameter<>("flag", 'e', "arg0", new AnnotationList()),
                new DummyParameter("dummy-1"),
                new PresenceFlagParameter<>("flag", 'x', "arg0", new AnnotationList())
        );
        final String rawInput = "-xce";
        final Matcher matcher = FlagGroup.VALID_PATTERN.matcher(rawInput);
        final boolean mathches = matcher.matches();
        assert mathches;

        final FlagGroup<Object> group = FlagGroup.parse(rawInput, matcher, parameters);
        int flagCount = 0;
        for (FlagParameter<Object> ignored : group) {
            flagCount++;
        }

        assertEquals(3, flagCount);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-xed", "-abc", "-asd", "-other"})
    public void parse_shorthandGroups_unrecognizedFlags(final String groupString) {
        final List<CommandParameter<Object>> parameters = List.of(
                new PresenceFlagParameter<>("flag", 'c', "arg0", new AnnotationList()),
                new DummyParameter("dummy-0"),
                new DummyParameter("dummy-1"),
                new PresenceFlagParameter<>("flag", 'e', "arg0", new AnnotationList()),
                new DummyParameter("dummy-1"),
                new PresenceFlagParameter<>("flag", 'x', "arg0", new AnnotationList())
        );

        final Matcher matcher = FlagGroup.VALID_PATTERN.matcher(groupString);
        final boolean mathches = matcher.matches();
        assert mathches;

        assertThrows(UnrecognizedFlagException.class, () -> FlagGroup.parse(groupString, matcher, parameters));
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
