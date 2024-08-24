package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;

import static java.lang.System.Logger.Level.INFO;

@FunctionalInterface
public interface Messenger<S> {

    void sendMessage(final @NotNull S source, final @NotNull String message);

    @SuppressWarnings("unchecked")
    static <S> @NotNull Messenger<S> builtin() {
        return (Messenger<S>) BuiltinMessenger.INSTANCE;
    }

    final class BuiltinMessenger<S> implements Messenger<S> {
        private static final System.Logger LOGGER = System.getLogger(BuiltinMessenger.class.getName());
        private static final Messenger<?> INSTANCE = new BuiltinMessenger<>();

        private BuiltinMessenger() {}

        @Override
        public void sendMessage(final @NotNull S source, final @NotNull String message) {
            LOGGER.log(INFO, message);
        }
    }
}
