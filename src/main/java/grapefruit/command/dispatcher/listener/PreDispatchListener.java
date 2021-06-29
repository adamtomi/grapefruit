package grapefruit.command.dispatcher.listener;

import grapefruit.command.dispatcher.CommandRegistration;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PreDispatchListener<S> {

    boolean onPreDispatch(final @NotNull S source, final @NotNull String commandLine, final @NotNull CommandRegistration<S> registration);
}
