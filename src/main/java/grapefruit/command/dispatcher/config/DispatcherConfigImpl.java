package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.dispatcher.CommandAuthorizer;
import grapefruit.command.dispatcher.CommandRegistrationHandler;
import grapefruit.command.util.function.ToBooleanFunction;

import static java.util.Objects.requireNonNull;

final class DispatcherConfigImpl<S> implements DispatcherConfig<S> {
    private final CommandAuthorizer<S> authorizer;
    private final CommandRegistrationHandler<S> registrationHandler;

    private DispatcherConfigImpl(final CommandAuthorizer<S> authorizer, final CommandRegistrationHandler<S> registrationHandler) {
        this.authorizer = requireNonNull(authorizer, "authorizer cannot be null");
        this.registrationHandler = requireNonNull(registrationHandler, "registrationHandler cannot be null");
    }

    @Override
    public CommandAuthorizer<S> authorizer() {
        return this.authorizer;
    }

    @Override
    public CommandRegistrationHandler<S> registrationHandler() {
        return this.registrationHandler;
    }

    static final class Builder<S> implements DispatcherConfig.Builder<S> {
        private CommandAuthorizer<S> authorizer;
        private CommandRegistrationHandler<S> registrationHandler;
        private ToBooleanFunction<CommandChain<S>> registrationFn;
        private ToBooleanFunction<CommandChain<S>> unregistrationFn;

        Builder() {}

        @Override
        public DispatcherConfig.Builder<S> authorize(final CommandAuthorizer<S> authorizer) {
            this.authorizer = requireNonNull(authorizer, "authorizer cannot be null");
            return this;
        }

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
        public DispatcherConfig<S> build() {
            final CommandAuthorizer<S> authorizer = this.authorizer != null
                    ? this.authorizer
                    : CommandAuthorizer.nil();

            final CommandRegistrationHandler<S> registrationHandler = this.registrationHandler != null
                    ? this.registrationHandler
                    : CommandRegistrationHandler.wrap(this.registrationFn, this.unregistrationFn);

            return new DispatcherConfigImpl<>(authorizer, registrationHandler);
        }
    }
}
