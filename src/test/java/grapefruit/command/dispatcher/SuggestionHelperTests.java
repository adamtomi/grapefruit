package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.PresenceFlagParameter;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.ValueFlagParameter;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMapperRegistry;
import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static grapefruit.command.dispatcher.SuggestionHelper.LAST_INPUT;
import static grapefruit.command.dispatcher.SuggestionHelper.SUGGEST_ME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SuggestionHelperTests {
    private static final List<String> PREFIXES;
    private static final List<String> NUMBER_OPTIONS;
    private static final List<String> BOOLEAN_OPTIONS =
            List.of("true", "t", "yes", "y", "allow", "1", "false", "f", "no", "n", "deny", "0");

    static {
        final List<Integer> prefixes = new ArrayList<>();
        for (int i = -9; i <= 9; i++) {
            // Don't wan't to start a number with '0'
            if (i != 0) {
                prefixes.add(i);
            }
        }

        PREFIXES = prefixes.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        final List<Integer> numberOptions = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            numberOptions.add(i);
        }

        NUMBER_OPTIONS = numberOptions.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @Test
    public void listSuggestions_emptyList_01() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                new StandardParameter<>("test0", false, TypeToken.of(Integer.TYPE), new AnnotationList(),
                    mapper(registry, TypeToken.of(Integer.class))),
                new StandardParameter<>("test1", false, TypeToken.of(Double.TYPE), new AnnotationList(),
                        mapper(registry, TypeToken.of(Double.class)))
        ));
        final CommandContext<Object> context = context(reg);
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        assertTrue(suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>()).isEmpty());
    }

    @Test
    public void listSuggestions_emptyList_02() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> param0 = new StandardParameter<>("test0", false, TypeToken.of(Integer.TYPE),
                new AnnotationList(), mapper(registry, TypeToken.of(Integer.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                param0,
                new StandardParameter<>("test1", false, TypeToken.of(Double.TYPE), new AnnotationList(),
                        mapper(registry, TypeToken.of(Double.class)))
        ));
        final CommandContext<Object> context = context(reg);
        context.put(SUGGEST_ME, param0);

        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        assertTrue(suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>()).isEmpty());
    }

    @Test
    public void listSuggestions_emptyInput() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> param0 = new StandardParameter<>("test0", false, TypeToken.of(Integer.TYPE),
                new AnnotationList(), mapper(registry, TypeToken.of(Integer.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                param0,
                new StandardParameter<>("test1", false, TypeToken.of(Boolean.TYPE), new AnnotationList(),
                        mapper(registry, TypeToken.of(Boolean.class)))
        ));
        final CommandContext<Object> context = context(reg);
        context.put(SUGGEST_ME, param0);
        context.put(LAST_INPUT, new StringCommandInput(""));
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> suggetsions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertEquals(PREFIXES, suggetsions);
    }

    @Test
    public void listSuggestions_withInput() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> param0 = new StandardParameter<>("test0", false, TypeToken.of(Integer.TYPE),
                new AnnotationList(), mapper(registry, TypeToken.of(Integer.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                param0,
                new StandardParameter<>("test1", false, TypeToken.of(Boolean.TYPE), new AnnotationList(),
                        mapper(registry, TypeToken.of(Boolean.class)))
        ));
        final CommandContext<Object> context = context(reg);
        context.put(SUGGEST_ME, param0);
        context.put(LAST_INPUT, new StringCommandInput("5"));
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> expected = NUMBER_OPTIONS.stream()
                .map(num -> "5" + num)
                .toList();
        final List<String> suggetsions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertEquals(expected, suggetsions);
    }

    @Test
    public void listSuggestions_withFlags_nonFlagInput() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> param0 = new StandardParameter<>("test0", false, TypeToken.of(Integer.TYPE),
                new AnnotationList(), mapper(registry, TypeToken.of(Integer.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                new PresenceFlagParameter<>("flag-0", ' ', "param0", new AnnotationList()),
                param0,
                new ValueFlagParameter<>("flag-1", ' ', "param1", TypeToken.of(String.class),
                        new AnnotationList(), mapper(registry, TypeToken.of(String.class)))
        ));
        final CommandContext<Object> context = context(reg);
        context.put(SUGGEST_ME, param0);
        context.put(LAST_INPUT, new StringCommandInput("-"));
        final List<String> expected = List.of(
                "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1",
                "1", "2", "3", "4", "5", "6", "7", "8", "9"
        );
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> suggestions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertEquals(expected, suggestions);
    }

    @Test
    public void listSuggestions_withFlags_numberInput() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> param0 = new StandardParameter<>("test0", false, TypeToken.of(Integer.TYPE),
                new AnnotationList(), mapper(registry, TypeToken.of(Integer.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                new PresenceFlagParameter<>("flag-0", ' ', "param0", new AnnotationList()),
                param0,
                new ValueFlagParameter<>("flag-1", ' ', "param1", TypeToken.of(String.class),
                        new AnnotationList(), mapper(registry, TypeToken.of(String.class)))
        ));
        final CommandContext<Object> context = context(reg);
        context.put(SUGGEST_ME, param0);
        context.put(LAST_INPUT, new StringCommandInput("-1"));
        final List<String> expected = List.of(
                "-10", "-11", "-12", "-13", "-14", "-15", "-16", "-17", "-18", "-19"
        );
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> suggestions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertEquals(expected, suggestions);
    }

    @Test
    public void listSuggestions_withFlags_flagLikeInput_01() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> flag = new ValueFlagParameter<>("flag-0", ' ', "param1",
                TypeToken.of(String.class), new AnnotationList(), mapper(registry, TypeToken.of(String.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                flag, new PresenceFlagParameter<>("flag-1", ' ', "param0", new AnnotationList())
        ));
        final CommandContext<Object> context = context(reg);

        context.put(SUGGEST_ME, flag);
        context.put(LAST_INPUT, new StringCommandInput("--"));
        final List<String> expected = List.of(
                "--flag-0", "--flag-1"
        );
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> suggestions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertEquals(expected, suggestions);
    }

    @Test
    public void listSuggestions_withFlags_flagInput() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> flag = new ValueFlagParameter<>("flag-0", ' ', "param1",
                TypeToken.of(Integer.TYPE), new AnnotationList(), mapper(registry, TypeToken.of(Integer.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                flag, new PresenceFlagParameter<>("flag-1", ' ', "param0", new AnnotationList())
        ));
        final CommandContext<Object> context = context(reg);

        context.put(SUGGEST_ME, flag);
        context.put(LAST_INPUT, new BlankCommandInput(1));
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> suggestions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertEquals(PREFIXES, suggestions);
    }

    @Test
    public void listSuggestions_withFlags_flagLikeInput_02() {
        final CommandParameter<Object> firstFlag =
                new PresenceFlagParameter<>("flag", 'f', "param0", new AnnotationList());
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                firstFlag,
                new PresenceFlagParameter<>("other", 'o', "param1", new AnnotationList()),
                new PresenceFlagParameter<>("another", 'a', "param2", new AnnotationList())
        ));
        final CommandContext<Object> context = context(reg);
        context.put(SUGGEST_ME, firstFlag);
        context.put(LAST_INPUT, new StringCommandInput("-"));
        final List<String> expected = List.of(
                "-f", "-a", "-o", "--flag", "--other", "--another", "-fa", "-fo"
        );
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> suggestions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertTrue(contentEquals(expected, suggestions));
    }

    @Test
    public void listSuggestions_withFlags_flagGroup_param01() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> firstFlag =
                new ValueFlagParameter<>("flag", 'f', "param0", TypeToken.of(Integer.TYPE),
                        new AnnotationList(), mapper(registry, TypeToken.of(Integer.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                firstFlag,
                new PresenceFlagParameter<>("other", 'o', "param1", new AnnotationList()),
                new ValueFlagParameter<>("another", 'a', "param2", TypeToken.of(Boolean.TYPE),
                        new AnnotationList(), mapper(registry, TypeToken.of(Boolean.class)))
        ));
        final CommandContext<Object> context = context(reg);
        context.put(SUGGEST_ME, firstFlag);
        context.put(LAST_INPUT, new BlankCommandInput(1));
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> suggestions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertEquals(PREFIXES, suggestions);
    }

    @Test
    public void listSuggestions_withFlags_flagGroup_param02() {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final CommandParameter<Object> flag = new ValueFlagParameter<>("another", 'a', "param2",
                TypeToken.of(Boolean.TYPE), new AnnotationList(), mapper(registry, TypeToken.of(Boolean.class)));
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                new ValueFlagParameter<>("flag", 'f', "param0", TypeToken.of(Integer.TYPE),
                        new AnnotationList(), mapper(registry, TypeToken.of(Integer.class))),
                new PresenceFlagParameter<>("other", 'o', "param1", new AnnotationList()),
                flag
        ));
        final CommandContext<Object> context = context(reg);
        context.put(SUGGEST_ME, flag);
        context.put(LAST_INPUT, new BlankCommandInput(1));
        final SuggestionHelper<Object> suggestionHelper = new SuggestionHelper<>();
        final List<String> suggestions = suggestionHelper.listSuggestions(context, reg, new PriorityQueue<>());
        assertTrue(contentEquals(BOOLEAN_OPTIONS, suggestions));
    }

    private static CommandContext<Object> context(final CommandRegistration<Object> reg) {
        return CommandContext.create(new Object(), "", reg.parameters());
    }

    private static <T> ParameterMapper<Object, T> mapper(final ParameterMapperRegistry<Object> registry,
                                                         final TypeToken<T> type) {
        final Optional<ParameterMapper<Object, T>> mapper = registry.findMapper(type);
        return mapper.orElseThrow();
    }

    private static <T> boolean contentEquals(final List<T> expected, List<T> actual) {
        for (final T t : expected) {
            if (!actual.contains(t)) {
                return false;
            }
        }

        return true;
    }

    private static final class DummyCommandRegistration implements CommandRegistration<Object> {
        private final List<CommandParameter<Object>> parameter;

        private DummyCommandRegistration(final List<CommandParameter<Object>> parameters) {
            this.parameter = parameters;
        }

        @Override
        public @NotNull CommandContainer holder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Method method() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull List<CommandParameter<Object>> parameters() {
            return this.parameter;
        }

        @Override
        public @NotNull Optional<String> permission() {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<TypeToken<?>> commandSourceType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean runAsync() {
            throw new UnsupportedOperationException();
        }
    }
}
