package grapefruit.command.dispatcher.exception;

import grapefruit.command.CommandException;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ExceptionHandler<S, X extends CommandException> {

    void handle(final @NotNull S source, final @NotNull String commandLine, final @NotNull X exception);
}
