package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.dispatcher.registration.RedirectingCommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.PresenceFlagParameter;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.ValueFlagParameter;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMapperRegistry;
import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandGraphTests {

    private static CommandGraph<Object> graph() {
        return new CommandGraph<>();
    }

    @ParameterizedTest
    @ValueSource(strings = {"root0|root1 sub0|sub1", "test0|test1", "some command path"})
    public void registerCommand_dummyRegistration(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration();
        assertDoesNotThrow(() -> graph.registerCommand(route, reg));
    }

    @Test
    public void registerCommand_emptyCommandRoute() {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration();
        assertThrows(IllegalArgumentException.class, () -> graph.registerCommand("", reg));
    }

    @ParameterizedTest
    @ValueSource(strings = {"root0|root1 sub0|sub1", "test0|test1", "some command path"})
    public void registerCommand_ambigiousTree(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg0 = new DummyCommandRegistration();
        final DummyCommandRegistration reg1 = new DummyCommandRegistration();
        graph.registerCommand(route, reg0);
        assertThrows(IllegalStateException.class, () -> graph.registerCommand(route, reg1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"$ ..|a", "\\\\ cc"})
    public void registerCommand_invalidNodeNames(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration();
        assertThrows(IllegalArgumentException.class, () -> graph.registerCommand(route, reg));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test0|test1", "some|command|path", "redirect", "root0", "sub1"})
    public void registerCommand_redirectingRegistration(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration();
        graph.registerCommand("root0|root1 sub0|sub1", reg);
        assertDoesNotThrow(() -> graph.registerCommand(route, new RedirectingCommandRegistration<>(reg, List.of())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"root0 ssub1", "test0", "some command path"})
    public void routeCommand_noSuchCommand(final String route) {
        final CommandGraph<Object> graph = graph();
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(route);
        final CommandGraph.RouteResult<?> result = graph.routeCommand(inputQueue);
        assertTrue(result instanceof CommandGraph.RouteResult.Failure<?> failure
                && failure.reason().equals(CommandGraph.RouteResult.Failure.Reason.NO_SUCH_COMMAND));
    }

    @ParameterizedTest
    @CsvSource({"root,root sub", "test,test child", "some,some command path"})
    public void routeCommand_invalidSyntax(final String commandLine, final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration();
        graph.registerCommand(route, reg);
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(commandLine);
        final CommandGraph.RouteResult<?> result = graph.routeCommand(inputQueue);
        assertTrue(result instanceof CommandGraph.RouteResult.Failure<?> failure
                && failure.reason().equals(CommandGraph.RouteResult.Failure.Reason.INVALID_SYNTAX));
    }

    @ParameterizedTest
    @CsvSource({
            "root|parent sub|child,root child",
            "test|test2|test3,test2",
            "some|someother command|othercommand path|otherpath,some othercommand path"
    })
    public void routeCommand_success(final String route, final String commandLine) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration();
        graph.registerCommand(route, reg);
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(commandLine);
        final CommandGraph.RouteResult<?> result = graph.routeCommand(inputQueue);
        assertTrue(result instanceof CommandGraph.RouteResult.Success<?> success
                && success.registration().equals(reg));
    }

    @ParameterizedTest
    @CsvSource({"root,root sub", "test,test child", "some,some command path"})
    public void routeCommand_redirectNode(final String redirectFrom, final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration();
        final RedirectingCommandRegistration<Object> redirectReg = new RedirectingCommandRegistration<>(reg, List.of());
        graph.registerCommand(route, reg);
        graph.registerCommand(redirectFrom, redirectReg);
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(redirectFrom);
        final CommandGraph.RouteResult<?> result = graph.routeCommand(inputQueue);
        assertTrue(result instanceof CommandGraph.RouteResult.Success<?> success
                && success.registration().equals(redirectReg));
    }

    @Test
    public void listSuggestions_emptyList() {
        final List<String> options = List.of("root", "test", "other");
        final CommandGraph<Object> graph = graph();
        options.forEach(route -> graph.registerCommand(route, new DummyCommandRegistration()));
        final List<String> result = graph.listSuggestions(new ConcurrentLinkedQueue<>());
        assertTrue(contentEquals(options, result));
    }

    @ParameterizedTest
    @CsvSource({
            "some|someother command|othercommand path|otherpath,some c,command|othercommand",
            "some|someother command|othercommand path|otherpath,some,command|othercommand",
            "some|someother command|othercommand path|otherpath,some othercommand ,path|otherpath"
    })
    public void listSuggestions_validRoute(final String route, final String input, final String expected) {
        final List<String> expectedElements = Arrays.asList(expected.split("\\|"));
        final CommandGraph<Object> graph = graph();
        graph.registerCommand(route, new DummyCommandRegistration());
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        final List<String> result = graph.listSuggestions(inputQueue);
        assertEquals(expectedElements, result);
    }

    @Test
    public void generateSyntaxFor_invalidInput() {
        final CommandGraph<Object> graph = graph();
        assertThrows(IllegalStateException.class, () -> graph.generateSyntaxFor(""));
    }

    @ParameterizedTest
    @CsvSource({
            "some command path$test$root child,some$test$root,''",
            "some command|othercommand|third$path$test$root child,some,command$othercommand$third",
            "some command path$test$root child,root,child"
    })
    public void generateSyntaxFor_validInput_noParameters(final String routesStr, final String expectedStr, final String commandLine) {
        final CommandGraph<Object> graph = graph();
        final List<String> routes = Arrays.asList(routesStr.split("\\$"));
        routes.forEach(route -> graph.registerCommand(route, new DummyCommandRegistration()));
        final String[] expected = expectedStr.split("\\$");
        final String syntax = graph.generateSyntaxFor(commandLine);

        boolean containsAll = true;
        for (final String each : expected) {
            if (!syntax.contains(each)) {
                containsAll = false;
                break;
            }
        }

        assertTrue(containsAll);
    }

    @Test
    public void generateSyntaxFor_validInput_parameters() {
        final CommandGraph<Object> graph = graph();
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(
                new StandardParameter<>("test", false, TypeToken.of(String.class), new AnnotationList(),
                    mapper(registry, TypeToken.of(String.class))),
                new StandardParameter<>("test2", false, TypeToken.of(Integer.TYPE), new AnnotationList(),
                        mapper(registry, TypeToken.of(Integer.class))),
                new StandardParameter<>("test3", true, TypeToken.of(Character.TYPE), new AnnotationList(),
                        mapper(registry, TypeToken.of(Character.class))),
                new PresenceFlagParameter<>("flag-0", ' ', "test4", new AnnotationList()),
                new PresenceFlagParameter<>("flag-1", 'f', "test5", new AnnotationList()),
                new ValueFlagParameter<>("flag-2", ' ', "test6", TypeToken.of(Long.TYPE), new AnnotationList(),
                        mapper(registry, TypeToken.of(Long.class)))
        ));
        graph.registerCommand("root", reg);
        final String expectedSyntax = "root <test> <test2> [test3] [--flag-0] [--flag-1] [--flag-2 test6]";
        final String actualSyntax = graph.generateSyntaxFor("root");
        assertEquals(expectedSyntax, actualSyntax);
    }

    private static <T> ParameterMapper<Object, T> mapper(final ParameterMapperRegistry<Object> registry,
                                                         final TypeToken<T> type) {
        final Optional<ParameterMapper<Object, T>> mapper = registry.findMapper(type);
        return mapper.orElseThrow();
    }

    private static boolean contentEquals(final List<String> expected, final List<String> actual) {
        for (final String element : expected) {
            if (!actual.contains(element)) {
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

        private DummyCommandRegistration() {
            this(List.of());
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
