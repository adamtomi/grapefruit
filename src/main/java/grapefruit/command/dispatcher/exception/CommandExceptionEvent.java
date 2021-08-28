package grapefruit.command.dispatcher.exception;

import org.jetbrains.annotations.NotNull;

public record CommandExceptionEvent<X extends Throwable, S> (@NotNull S source,
                                                             @NotNull String commandLine,
                                                             @NotNull X exception) {
}
