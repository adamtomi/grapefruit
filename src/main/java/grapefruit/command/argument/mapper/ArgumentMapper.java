package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import io.leangen.geantyref.TypeToken;

public interface ArgumentMapper<S, T> {

    TypeToken<T> type();

    boolean isTerminal();

    T tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws CommandException;

    static <S, T> ArgumentMapper<S, T> constant(final Class<T> clazz, final T value) {
        return new AbstractArgumentMapper<>(TypeToken.get(clazz), false) {
            @Override
            public T tryMap(final CommandContext<S> context, final CommandInputTokenizer input) {
                return value;
            }
        };
    }

    static <S, T> ArgumentMapper<S, T> constant(final TypeToken<T> type, final T value) {
        return new AbstractArgumentMapper<>(type, false) {
            @Override
            public T tryMap(final CommandContext<S> context, final CommandInputTokenizer input) {
                return value;
            }
        };
    }
}
