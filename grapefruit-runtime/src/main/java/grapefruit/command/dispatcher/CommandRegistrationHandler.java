package grapefruit.command.dispatcher;

import grapefruit.command.Command;

import java.io.Serial;

/**
 * Provides a simple way for 3rd parties to handle
 * command registration/unregistration. If those
 * actions are undesired for some reason, the implementation
 * can call {@link this#interrupt()} to interrupt the registration
 * or unregistration of the command.
 */
public abstract class CommandRegistrationHandler {

    /**
     * Implementations may decide to perform some
     * logic upon command registration.
     *
     * @param command The command being registered
     */
    public abstract void onRegister(Command command);

    /**
     * Implementations may decide to perform some
     * logic upon command unregistration.
     *
     * @param command The command being unregistered
     */
    public abstract void onUnregister(Command command);

    /**
     * Utility function to interrupt the ongoing
     * registration/unregistration.
     *
     * @throws Interrupt When the method is called
     */
    protected final void interrupt() {
        throw new Interrupt();
    }

    /**
     * Returns a no operation registartion handler.
     *
     * @return The handler instance
     */
    public static CommandRegistrationHandler noop() {
        return NoOp.INSTANCE;
    }

    /* Dummy exception */
    static final class Interrupt extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -1364401961627181390L;

        Interrupt() {
            super("Interrupting command registration");
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    /* NOOP implementation */
    static final class NoOp extends CommandRegistrationHandler {
        private static final NoOp INSTANCE = new NoOp();

        private NoOp() {}

        @Override
        public void onRegister(Command command) {}

        @Override
        public void onUnregister(Command command) {}
    }
}
