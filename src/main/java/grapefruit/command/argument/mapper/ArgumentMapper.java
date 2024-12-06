package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import io.leangen.geantyref.TypeToken;

public interface ArgumentMapper<S, T> {

    TypeToken<T> type();

    boolean isTerminal();

    T tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws CommandException;

    static <S, T> ArgumentMapper<S, T> constant(Class<T> clazz, T value) {
        return new AbstractArgumentMapper<>(TypeToken.get(clazz), false) {
            @Override
            public T tryMap(final CommandContext<S> context, final CommandInputTokenizer input) {
                return value;
            }
        };
    }

    static <S, T> ArgumentMapper<S, T> constant(TypeToken<T> type, T value) {
        return new AbstractArgumentMapper<>(type, false) {
            @Override
            public T tryMap(CommandContext<S> context, CommandInputTokenizer input) {
                return value;
            }
        };
    }
}
