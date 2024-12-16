package grapefruit.command.argument.mapper;

import grapefruit.command.dispatcher.CommandContext;
import io.leangen.geantyref.TypeToken;

import java.util.List;

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
    public List<String> complete(final CommandContext<S> context, final String input) {
        return List.of();
    }
}
