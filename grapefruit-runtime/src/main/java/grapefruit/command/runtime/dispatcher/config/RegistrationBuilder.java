package grapefruit.command.runtime.dispatcher.config;

import grapefruit.command.runtime.Command;
import grapefruit.command.runtime.dispatcher.CommandRegistrationHandler;

import java.util.function.Consumer;

/**
 * Builder class to simplify building command registration handlers.
 */
public interface RegistrationBuilder {

    /**
     * If more complex logic is to be performed upon command registration
     * / unregistration, it is advised to extends {@link CommandRegistrationHandler}
     * and pass an instance of the custom implementation to this method.
     *
     * @param handler The handler implementation instance
     */
    void using(CommandRegistrationHandler handler);

    /**
     * In some cases even one-liners might be enough to perform registration
     * logic. In such cases this builder method can come in handy.
     *
     * @param stage The registration stage
     * @param handler The registration handler
     * @return A newly created {@link Lambda} instance
     */
    Lambda on(Stage stage, Consumer<Command> handler);

    /**
     * Utility class to create one-liner ("lambda") registration handlers.
     */
    interface Lambda {

        /**
         * @see RegistrationBuilder#on(Stage, Consumer)
         */
        Lambda on(Stage stage, Consumer<Command> handler);

        /**
         * Registers the registration handler.
         */
        void use();
    }

    /**
     * Registration stages.
     */
    enum Stage {
        REGISTERING, UNREGISTRING
    }
}
