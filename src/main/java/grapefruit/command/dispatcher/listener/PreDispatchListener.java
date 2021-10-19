package grapefruit.command.dispatcher.listener;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PreDispatchListener<S> {

    boolean onPreDispatch(final @NotNull CommandContext<S> context, final @NotNull CommandRegistration<S> registration);
}
