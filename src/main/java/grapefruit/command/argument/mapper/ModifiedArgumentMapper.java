package grapefruit.command.argument.mapper;

import grapefruit.command.completion.CommandCompletion;
import grapefruit.command.completion.CompletionBuilder;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;
import io.leangen.geantyref.TypeToken;

import static java.util.Objects.requireNonNull;

final class ModifiedArgumentMapper<S, I, O> extends AbstractArgumentMapper<S, O> {
    private final ArgumentMapper<S, I> delegate;
    private final Modifier<S, I, O> modifier;

    ModifiedArgumentMapper(final ArgumentMapper<S, I> delegate, final Modifier<S, I, O> modifier) {
        super(
                new TypeToken<>() {},
                requireNonNull(delegate, "delegate cannot be null").isTerminal()
        );
        this.delegate = delegate;
        this.modifier = requireNonNull(modifier, "modifier cannot be null");
    }

    @Override
    public O tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws ArgumentMappingException, MissingInputException {
        final I value = this.delegate.tryMap(context, input);
        return this.modifier.modify(context, value);
    }

    @Override
    public CommandCompletion complete(final CommandContext<S> context, final CompletionBuilder builder) {
        return this.delegate.complete(context, builder);
    }
}
