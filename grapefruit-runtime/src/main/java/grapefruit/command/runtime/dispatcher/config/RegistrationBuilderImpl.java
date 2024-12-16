package grapefruit.command.runtime.dispatcher.config;

import grapefruit.command.runtime.dispatcher.CommandRegistrationHandler;
import grapefruit.command.runtime.generated.CommandMirror;

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
    public RegistrationBuilder.Lambda on(Stage stage, Consumer<CommandMirror> handler) {
        requireNonNull(stage, "stage cannot be null");
        requireNonNull(handler, "handler cannot be null");
        Lambda builder = new Lambda(this.handler);
        builder.consumers.put(stage, handler);
        return builder;
    }

    static final class Lambda implements RegistrationBuilder.Lambda {
        private final Consumer<CommandRegistrationHandler> handler;
        private final Map<Stage, Consumer<CommandMirror>> consumers = new HashMap<>();

        Lambda(Consumer<CommandRegistrationHandler> handler) {
            this.handler = requireNonNull(handler, "handler cannot be null");
        }

        @Override
        public Lambda on(Stage stage, Consumer<CommandMirror> handler) {
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

    static final class ConsumerBackedRegistrationHandler implements CommandRegistrationHandler {
        private final Consumer<CommandMirror> registerHandler;
        private final Consumer<CommandMirror> unregisterHandler;

        ConsumerBackedRegistrationHandler(Consumer<CommandMirror> registerHandler, Consumer<CommandMirror> unregisterHandler) {
            this.registerHandler = requireNonNull(registerHandler, "registerHandler cannot be null");
            this.unregisterHandler = requireNonNull(unregisterHandler, "unregisterHandler cannot be null");
        }

        @Override
        public boolean register(CommandMirror command) {
            this.registerHandler.accept(command);
            return true;
        }

        @Override
        public boolean unregister(CommandMirror command) {
            this.unregisterHandler.accept(command);
            return true;
        }
    }
}
