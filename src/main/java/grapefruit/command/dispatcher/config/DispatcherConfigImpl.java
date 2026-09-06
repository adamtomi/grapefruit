package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.completion.CommandCompletion;
import grapefruit.command.completion.CompletionFactory;
import grapefruit.command.dispatcher.CommandRegistrationHandler;
import grapefruit.command.dispatcher.ContextInjector;
import grapefruit.command.suggestion.SuggestionFactory;
import grapefruit.command.util.function.ToBooleanFunction;

import static java.util.Objects.requireNonNull;

final class DispatcherConfigImpl<S> implements DispatcherConfig<S> {
    private final CommandRegistrationHandler<S> registrationHandler;
    private final ContextInjector<S> contextInjector;
    private final CompletionFactory completionFactory;
    private final SuggestionFactory suggestionFactory;
    private final boolean eagerFlagCompletions;

    private DispatcherConfigImpl(
            final CommandRegistrationHandler<S> registrationHandler,
            final ContextInjector<S> contextInjector,
            final CompletionFactory completionFactory,
            final SuggestionFactory suggestionFactory,
            final boolean eagerFlagCompletions
    ) {
        this.registrationHandler = requireNonNull(registrationHandler, "registrationHandler cannot be null");
        this.contextInjector = requireNonNull(contextInjector, "contextInjector cannot be null");
        this.completionFactory = requireNonNull(completionFactory, "completionFactory cannot be null");
        this.suggestionFactory = requireNonNull(suggestionFactory, "suggestionFactory cannot be null");
        this.eagerFlagCompletions = eagerFlagCompletions;
    }

    @Override
    public CommandRegistrationHandler<S> registrationHandler() {
        return this.registrationHandler;
    }

    @Override
    public ContextInjector<S> contextInjector() {
        return this.contextInjector;
    }

    @Override
    public CompletionFactory completionFactory() {
        return this.completionFactory;
    }

    @Override
    public SuggestionFactory suggestionFactory() {
        return this.suggestionFactory;
    }

    @Override
    public boolean eagerFlagSuggestions() {
        return this.eagerFlagCompletions;
    }

    static final class Builder<S> implements DispatcherConfig.Builder<S> {
        private CommandRegistrationHandler<S> registrationHandler;
        private ToBooleanFunction<CommandChain<S>> registrationFn;
        private ToBooleanFunction<CommandChain<S>> unregistrationFn;
        private ContextInjector<S> contextInjector;
        private CompletionFactory completionFactory;
        private SuggestionFactory suggestionFactory;
        private boolean eagerFlagCompletions;

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
        public DispatcherConfig.Builder<S> contextInjector(final ContextInjector<S> handler) {
            this.contextInjector = requireNonNull(handler, "handler cannot be null");
            return this;
        }

        @Override
        public DispatcherConfig.Builder<S> completionFactory(final CompletionFactory factory) {
            this.completionFactory = requireNonNull(factory, "factory cannot be null");
            return this;
        }

        @Override
        public DispatcherConfig.Builder<S> suggestionFactory(final SuggestionFactory factory) {
            this.suggestionFactory = requireNonNull(factory, "factory cannot be null");
            return this;
        }

        @Override
        public DispatcherConfig.Builder<S> eagerFlagCompletions() {
            this.eagerFlagCompletions = true;
            return this;
        }

        @Override
        public DispatcherConfig<S> build() {
            final CommandRegistrationHandler<S> registrationHandler = this.registrationHandler != null
                    ? this.registrationHandler
                    : CommandRegistrationHandler.wrap(this.registrationFn, this.unregistrationFn);

            final ContextInjector<S> contextInjector = this.contextInjector != null
                    ? this.contextInjector
                    : ContextInjector.noop();

            final CompletionFactory completionFactory = this.completionFactory != null
                    ? this.completionFactory
                    : CommandCompletion.factory();

            final SuggestionFactory suggestionFactory = this.suggestionFactory != null
                    ? this.suggestionFactory
                    : SuggestionFactory.defaultFactory();

            return new DispatcherConfigImpl<>(registrationHandler, contextInjector, completionFactory, suggestionFactory, this.eagerFlagCompletions);
        }
    }
}
