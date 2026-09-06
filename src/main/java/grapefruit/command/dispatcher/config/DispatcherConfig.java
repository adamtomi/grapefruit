package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.dispatcher.CommandRegistrationHandler;
import grapefruit.command.dispatcher.ContextInjector;
import grapefruit.command.suggestion.SuggestionFactory;
import grapefruit.command.util.function.ToBooleanFunction;

public interface DispatcherConfig<S> {

    CommandRegistrationHandler<S> registrationHandler();

    ContextInjector<S> contextInjector();

    SuggestionFactory suggestionFactory();

    boolean eagerFlagSuggestions();

    static <S> Builder<S> builder() {
        return new DispatcherConfigImpl.Builder<>();
    }

    interface Builder<S> {

        Builder<S> registrations(final CommandRegistrationHandler<S> handler);

        Builder<S> register(final ToBooleanFunction<CommandChain<S>> handler);

        Builder<S> unregister(final ToBooleanFunction<CommandChain<S>> handler);

        Builder<S> contextInjector(final ContextInjector<S> handler);

        Builder<S> suggestionFactory(final SuggestionFactory factory);

        Builder<S> eagerFlagSuggestions();

        DispatcherConfig<S> build();
    }
}
