package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.dispatcher.CommandRegistrationHandler;
import grapefruit.command.util.function.ToBooleanFunction;

public interface DispatcherConfig<S> {

    CommandRegistrationHandler<S> registrationHandler();

    static <S> Builder<S> builder() {
        return new DispatcherConfigImpl.Builder<>();
    }

    interface Builder<S> {

        Builder<S> registrations(final CommandRegistrationHandler<S> handler);

        Builder<S> register(final ToBooleanFunction<CommandChain<S>> handler);

        Builder<S> unregister(final ToBooleanFunction<CommandChain<S>> handler);

        DispatcherConfig<S> build();
    }
}
