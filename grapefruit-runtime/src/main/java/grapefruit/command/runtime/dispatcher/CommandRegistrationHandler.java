package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.generated.CommandMirror;

/**
 * Allows third parties to run some logic upon the
 * registration or unregistration of a certain command.
 */
public interface CommandRegistrationHandler {

    /**
     * This method is invoked before the supplied {@link CommandMirror}
     * is registered into the command tree. If the registration
     * process should not proceed any further, implementations
     * may return {@code false}.
     *
     * @param command The command being registered
     * @return Whether to register the command or not
     */
    boolean register(CommandMirror command);

    /**
     * This method is invoked before the supplied {@link CommandMirror}
     * is unregistered from the command tree. If the unregistration
     * process should not proceed any further, implementations
     * may return {@code false}.
     *
     * @param command The command being unregistered
     * @return Whether to unregister the command or not
     */
    boolean unregister(CommandMirror command);

    /**
     * Returns a no operation registartion handler.
     *
     * @return The handler instance
     */
    static CommandRegistrationHandler noop() {
        return NoOp.INSTANCE;
    }

    /* NOOP implementation */
    final class NoOp implements CommandRegistrationHandler {
        private static final NoOp INSTANCE = new NoOp();

        private NoOp() {}

        @Override
        public boolean register(CommandMirror command) {
            return true;
        }

        @Override
        public boolean unregister(CommandMirror command) {
            return true;
        }
    }
}
