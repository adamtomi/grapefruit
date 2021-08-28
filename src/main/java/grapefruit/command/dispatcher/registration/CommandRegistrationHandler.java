package grapefruit.command.dispatcher.registration;

import java.util.function.Consumer;

@FunctionalInterface
public interface CommandRegistrationHandler<S> extends Consumer<CommandRegistrationContext<S>> {

    CommandRegistrationHandler<?> NO_OP = context -> {};
}
