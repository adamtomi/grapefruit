package grapefruit.command.dispatcher.listener;

import grapefruit.command.dispatcher.CommandResult;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PostDispatchListener<S> {

    void onPostDispatch(final @NotNull S source, final @NotNull CommandResult result);
}
