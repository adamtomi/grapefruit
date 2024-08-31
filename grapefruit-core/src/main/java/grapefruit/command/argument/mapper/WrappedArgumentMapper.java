package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import static java.util.Objects.requireNonNull;

public final class WrappedArgumentMapper<T> implements ArgumentMapper<T> {
    private final ArgumentMapper<T> delegate;

    private WrappedArgumentMapper(ArgumentMapper<T> delegate) {
        this.delegate = requireNonNull(delegate, "delegate cannot be null");
    }

    public static <T> ArgumentMapper<T> of(ArgumentMapper<T> delegate) {
        return new WrappedArgumentMapper<>(delegate);
    }

    @Override
    public T tryMap(CommandContext context, StringReader reader) throws CommandException {
        return this.delegate.tryMap(context, reader);
    }
}
