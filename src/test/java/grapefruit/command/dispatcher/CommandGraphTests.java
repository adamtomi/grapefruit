package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.dispatcher.registration.RedirectingCommandRegistration;
import grapefruit.command.parameter.CommandParameter;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandGraphTests {

    private static CommandGraph<Object> graph() {
        return new CommandGraph<>((source, permission) -> true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"root0|root1 sub0|sub1", "test0|test1", "some command path"})
    public void registerCommand_dummyRegistration(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of());
        assertDoesNotThrow(() -> graph.registerCommand(route, reg));
    }

    @Test
    public void registerCommand_emptyCommandRoute() {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of());
        assertThrows(IllegalArgumentException.class, () -> graph.registerCommand("", reg));
    }

    @ParameterizedTest
    @ValueSource(strings = {"root0|root1 sub0|sub1", "test0|test1", "some command path"})
    public void registerCommand_ambigiousTree(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg0 = new DummyCommandRegistration(List.of());
        final DummyCommandRegistration reg1 = new DummyCommandRegistration(List.of());
        graph.registerCommand(route, reg0);
        assertThrows(IllegalStateException.class, () -> graph.registerCommand(route, reg1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"$ ..|a", "\\\\ cc"})
    public void registerCommand_invalidNodeNames(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of());
        assertThrows(IllegalArgumentException.class, () -> graph.registerCommand(route, reg));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test0|test1", "some|command|path", "redirect", "root0", "sub1"})
    public void registerCommand_redirectingRegistration(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of());
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
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of());
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
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of());
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
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of());
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
        options.forEach(route -> graph.registerCommand(route, new DummyCommandRegistration(List.of())));
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
        graph.registerCommand(route, new DummyCommandRegistration(List.of()));
        final CommandInputTokenizer tokenizer = new CommandInputTokenizer();
        final Queue<CommandInput> inputQueue = tokenizer.tokenizeInput(input);
        final List<String> result = graph.listSuggestions(inputQueue);
        assertEquals(expectedElements, result);
    }

    public void generateSyntaxFor_invalidInput() {}

    public void generateSyntaxFor_validInput_01() {}

    public void generateSyntaxFor_validInput_02() {}

    public void generateSyntaxFor_validInput_03() {}

    private static boolean contentEquals(final List<String> expected, final List<String> actual) {
        for (final String element : expected) {
            if (!actual.contains(element)) {
                return false;
            }
        }

        return true;
    }

    private static final class DummyCommandRegistration implements CommandRegistration<Object> {
        private final List<CommandParameter<Object>> parameters;

        private DummyCommandRegistration(final @NotNull List<CommandParameter<Object>> params) {
            this.parameters = params;
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
            return this.parameters;
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
