package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class SuggestionHelperTests {

    @Test
    public void listSuggestions_emptyList() {
        final DummyCommandRegistration reg = new DummyCommandRegistration(List.of(

        ));
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
