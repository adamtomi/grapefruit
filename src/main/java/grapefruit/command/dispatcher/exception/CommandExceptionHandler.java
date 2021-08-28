package grapefruit.command.dispatcher.exception;

import java.util.function.Consumer;

@FunctionalInterface
public interface CommandExceptionHandler<X extends Throwable, S> extends Consumer<CommandExceptionEvent<X, S>> {}
