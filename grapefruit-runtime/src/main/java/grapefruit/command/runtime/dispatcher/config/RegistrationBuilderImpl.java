package grapefruit.command.runtime.dispatcher.config;

import grapefruit.command.runtime.Command;
import grapefruit.command.runtime.dispatcher.CommandRegistrationHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

final class RegistrationBuilderImpl implements RegistrationBuilder {
    private final Consumer<CommandRegistrationHandler> handler;

    RegistrationBuilderImpl(Consumer<CommandRegistrationHandler> handler) {
        this.handler = requireNonNull(handler, "handler cannot be null");
    }

    @Override
    public void using(CommandRegistrationHandler handler) {
        requireNonNull(handler, "handler cannot be null");
        this.handler.accept(handler);
    }

    @Override
    public RegistrationBuilder.Lambda on(Stage stage, Consumer<Command> handler) {
        requireNonNull(stage, "stage cannot be null");
        requireNonNull(handler, "handler cannot be null");
        Lambda builder = new Lambda(this.handler);
        builder.consumers.put(stage, handler);
        return builder;
    }

    static final class Lambda implements RegistrationBuilder.Lambda {
        private final Consumer<CommandRegistrationHandler> handler;
        private final Map<Stage, Consumer<Command>> consumers = new HashMap<>();

        Lambda(Consumer<CommandRegistrationHandler> handler) {
            this.handler = requireNonNull(handler, "handler cannot be null");
        }

        @Override
        public Lambda on(Stage stage, Consumer<Command> handler) {
            requireNonNull(stage, "stage cannot be null");
            requireNonNull(handler, "handler cannot be null");
            this.consumers.put(stage, handler);

            return this;
        }

        @Override
        public void use() {
            this.handler.accept(new ConsumerBackedRegistrationHandler(
                    this.consumers.getOrDefault(Stage.REGISTERING, command -> {}),
                    this.consumers.getOrDefault(Stage.UNREGISTRING, command -> {})
            ));
        }
    }

    static final class ConsumerBackedRegistrationHandler extends CommandRegistrationHandler {
        private final Consumer<Command> registerHandler;
        private final Consumer<Command> unregisterHandler;

        ConsumerBackedRegistrationHandler(Consumer<Command> registerHandler, Consumer<Command> unregisterHandler) {
            this.registerHandler = requireNonNull(registerHandler, "registerHandler cannot be null");
            this.unregisterHandler = requireNonNull(unregisterHandler, "unregisterHandler cannot be null");
        }

        @Override
        public void onRegister(Command command) {
            this.registerHandler.accept(command);
        }

        @Override
        public void onUnregister(Command command) {
            this.unregisterHandler.accept(command);
        }
    }
}
