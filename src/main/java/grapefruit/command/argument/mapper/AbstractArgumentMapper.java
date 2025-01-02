package grapefruit.command.argument.mapper;

import grapefruit.command.completion.CompletionBuilder;
import grapefruit.command.completion.CommandCompletion;
import grapefruit.command.dispatcher.CommandContext;
import io.leangen.geantyref.TypeToken;

import static java.util.Objects.requireNonNull;

public abstract class AbstractArgumentMapper<S, T> implements ArgumentMapper<S, T> {
    private final TypeToken<T> type;
    private final boolean isTerminal;

    protected AbstractArgumentMapper(final TypeToken<T> type, final boolean isTerminal) {
        this.type = requireNonNull(type, "type cannot be null");
        this.isTerminal = isTerminal;
    }

    protected AbstractArgumentMapper(final Class<T> type, final boolean isTerminal) {
        this(TypeToken.get(requireNonNull(type, "type cannot be null")), isTerminal);
    }

    @Override
    public TypeToken<T> type() {
        return this.type;
    }

    @Override
    public boolean isTerminal() {
        return this.isTerminal;
    }

    @Override
    public CommandCompletion complete(final CommandContext<S> context, final CompletionBuilder builder) {
        return CommandCompletion.none();
    }

    @Override
    public <O> ArgumentMapper<S, O> mapping(final Modifier<S, T, O> modifier) {
        return new ModifiedArgumentMapper<>(this, modifier);
    }

    @Override
    public ArgumentMapper<S, T> filtering(final Filter<S, T> filter) {
        return new ModifiedArgumentMapper<>(this, filter);
    }
}
