package grapefruit.command.dispatcher.listener;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PreProcessLitener<S> {

    boolean onPreProcess(final @NotNull S source, final @NotNull String commandLine);
}
