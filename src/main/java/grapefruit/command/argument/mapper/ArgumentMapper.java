package grapefruit.command.argument.mapper;

import grapefruit.command.completion.CompletionProvider;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;
import io.leangen.geantyref.TypeToken;

public interface ArgumentMapper<S, T> extends CompletionProvider<S> {

    TypeToken<T> type();

    boolean isTerminal();

    T tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws ArgumentMappingException, MissingInputException;

    @FunctionalInterface
    interface Modifier<S, I, O> {

        O modify(final CommandContext<S> context, final I input) throws ArgumentMappingException;
    }

    @FunctionalInterface
    interface Filter<S, T> extends Modifier<S, T, T> {

        void test(final CommandContext<S> context, final T input) throws ArgumentMappingException;

        @Override
        default T modify(final CommandContext<S> context, final T input) throws ArgumentMappingException {
            test(context, input);
            return input;
        }
    }

    <O> ArgumentMapper<S, O> mapping(final Modifier<S, T, O> modifier);

    ArgumentMapper<S, T> filtering(final Filter<S, T> filter);
}
