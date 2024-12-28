package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;
import io.leangen.geantyref.TypeToken;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface ArgumentMapper<S, T> {

    TypeToken<T> type();

    boolean isTerminal();

    T tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws ArgumentMappingException, MissingInputException;

    List<String> complete(final CommandContext<S> context, final String input);

    default ArgumentMapper<S, T> with(final Collection<Filter<S, T>> filters) {
        return new ArgumentMapper<>() {
            @Override
            public TypeToken<T> type() {
                return ArgumentMapper.this.type();
            }

            @Override
            public boolean isTerminal() {
                return ArgumentMapper.this.isTerminal();
            }

            @Override
            public T tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws ArgumentMappingException, MissingInputException {
                final T value = ArgumentMapper.this.tryMap(context, input);
                for (final Filter<S, T> filter : filters) {
                    if (!filter.test(context, value)) {
                        // TODO
                        // throw access.wrapException(filter.generateException(context, value));
                    }
                }

                return value;
            }

            @Override
            public List<String> complete(final CommandContext<S> context, final String input) {
                return ArgumentMapper.this.complete(context, input);
            }
        };
    }

    default ArgumentMapper<S, T> with(final Filter<S, T> filter) {
        return with(List.of(filter));
    }

    interface Filter<S, T> {

        boolean test(final CommandContext<S> context, final T value);

        CommandException generateException(final CommandContext<S> context, final T value);

        @FunctionalInterface
        interface ExceptionFactory<S, T> {

            CommandException create(final CommandContext<S> context, final T value);

            static <S, T> ExceptionFactory<S, T> contextFree(final Supplier<CommandException> supplier) {
                return (context, value) -> supplier.get();
            }
        }
    }
}
