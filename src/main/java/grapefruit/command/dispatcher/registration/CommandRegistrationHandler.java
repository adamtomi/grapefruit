package grapefruit.command.dispatcher.registration;

import org.jetbrains.annotations.NotNull;

public interface CommandRegistrationHandler<S> {

    void register(final @NotNull CommandRegistration<S> reg);

    void unregister(final @NotNull CommandRegistration<S> reg, final boolean fullUnregister);

    CommandRegistrationHandler<?> NO_OP = NoOpRegistrationHandler.INSTANCE;

    class NoOpRegistrationHandler<S> implements CommandRegistrationHandler<S> {
        private static final NoOpRegistrationHandler<?> INSTANCE = new NoOpRegistrationHandler<>();

        private NoOpRegistrationHandler() {}

        @Override
        public void register(final @NotNull CommandRegistration<S> reg) {
            // Do nothing
        }

        @Override
        public void unregister(final @NotNull CommandRegistration<S> reg, final boolean fullUnregister) {
            // Do nothing
        }
    }
}
