package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.dispatcher.registration.RedirectingCommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

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
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(), null);
        assertDoesNotThrow(() -> graph.registerCommand(route, reg));
    }

    @Test
    public void registerCommand_emptyCommandRoute() {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(), null);
        assertThrows(IllegalArgumentException.class, () -> graph.registerCommand("", reg));
    }

    @ParameterizedTest
    @ValueSource(strings = {"root0|root1 sub0|sub1", "test0|test1", "some command path"})
    public void registerCommand_ambigiousTree(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg0 = new DummyCommandRegistration(List.of(), null);
        final DummyCommandRegistration reg1 = new DummyCommandRegistration(List.of(), null);
        graph.registerCommand(route, reg0);
        assertThrows(IllegalStateException.class, () -> graph.registerCommand(route, reg1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"$ ..|a", "\\\\ cc"})
    public void registerCommand_invalidNodeNames(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(), null);
        assertThrows(IllegalArgumentException.class, () -> graph.registerCommand(route, reg));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test0|test1", "some|command|path", "redirect", "root0", "sub1"})
    public void registerCommand_redirectingRegistration(final String route) {
        final CommandGraph<Object> graph = graph();
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(), null);
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

    public void routeCommand_invalidSyntax() {}

    public void routeCommand_success() {}

    public void routeCommand_redirectNode() {}

    public void listSuggestions_emptyList() {}

    public void listSuggestions_validRoute() {}

    // TODO test listSuggestions for command registrations

    public void generateSyntaxFor_invalidInput() {}

    public void generateSyntaxFor_validInput_01() {}

    public void generateSyntaxFor_validInput_02() {}

    public void generateSyntaxFor_validInput_03() {}

    private static final class DummyCommandRegistration implements CommandRegistration<Object> {
        private final List<CommandParameter<Object>> parameters;
        private final @Nullable String permission;

        private DummyCommandRegistration(final @NotNull List<CommandParameter<Object>> params,
                                         final @Nullable String permission) {
            this.parameters = params;
            this.permission = permission;
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
            return Optional.ofNullable(this.permission);
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
