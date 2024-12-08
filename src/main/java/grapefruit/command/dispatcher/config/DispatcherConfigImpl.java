package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.dispatcher.CommandRegistrationHandler;
import grapefruit.command.dispatcher.ContextDecorator;
import grapefruit.command.util.function.ToBooleanFunction;

import static java.util.Objects.requireNonNull;

final class DispatcherConfigImpl<S> implements DispatcherConfig<S> {
    private final CommandRegistrationHandler<S> registrationHandler;
    private final ContextDecorator<S> contextDecorator;

    private DispatcherConfigImpl(final CommandRegistrationHandler<S> registrationHandler, final ContextDecorator<S> contextDecorator) {
        this.registrationHandler = requireNonNull(registrationHandler, "registrationHandler cannot be null");
        this.contextDecorator = requireNonNull(contextDecorator, "contextDecorator cannot be null");
    }

    @Override
    public CommandRegistrationHandler<S> registrationHandler() {
        return this.registrationHandler;
    }

    @Override
    public ContextDecorator<S> contextDecorator() {
        return this.contextDecorator;
    }

    static final class Builder<S> implements DispatcherConfig.Builder<S> {
        private CommandRegistrationHandler<S> registrationHandler;
        private ToBooleanFunction<CommandChain<S>> registrationFn;
        private ToBooleanFunction<CommandChain<S>> unregistrationFn;
        private ContextDecorator<S> contextDecorator;

        Builder() {}

        @Override
        public DispatcherConfig.Builder<S> registrations(final CommandRegistrationHandler<S> handler) {
            this.registrationHandler = requireNonNull(handler, "handler cannot be null");
            return this;
        }

        @Override
        public DispatcherConfig.Builder<S> register(final ToBooleanFunction<CommandChain<S>> handler) {
            this.registrationFn = requireNonNull(handler, "handler cannot be null");
            return this;
        }

        @Override
        public DispatcherConfig.Builder<S> unregister(final ToBooleanFunction<CommandChain<S>> handler) {
            this.unregistrationFn = requireNonNull(handler, "handler cannot be null");
            return this;
        }

        @Override
        public DispatcherConfig.Builder<S> decorateContext(final ContextDecorator<S> handler) {
            this.contextDecorator = requireNonNull(handler, "handler cannot be null");
            return this;
        }

        @Override
        public DispatcherConfig<S> build() {
            final CommandRegistrationHandler<S> registrationHandler = this.registrationHandler != null
                    ? this.registrationHandler
                    : CommandRegistrationHandler.wrap(this.registrationFn, this.unregistrationFn);

            final ContextDecorator<S> contextDecorator = this.contextDecorator != null
                    ? this.contextDecorator
                    : (context, mode) -> {};

            return new DispatcherConfigImpl<>(registrationHandler, contextDecorator);
        }
    }
}
