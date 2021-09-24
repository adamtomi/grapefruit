package grapefruit.command.dispatcher.listener;

import grapefruit.command.dispatcher.CommandContext;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PostDispatchListener<S> {

    void onPostDispatch(final @NotNull CommandContext<S> context);
}
