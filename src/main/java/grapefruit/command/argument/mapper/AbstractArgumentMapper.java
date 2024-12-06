package grapefruit.command.argument.mapper;

import io.leangen.geantyref.TypeToken;

import static java.util.Objects.requireNonNull;

public abstract class AbstractArgumentMapper<S, T> implements ArgumentMapper<S, T> {
    private final TypeToken<T> type;
    private final boolean isTerminal;

    protected AbstractArgumentMapper(final TypeToken<T> type, final boolean isTerminal) {
        this.type = requireNonNull(type, "type cannot be null");
        this.isTerminal = isTerminal;
    }

    @Override
    public TypeToken<T> type() {
        return this.type;
    }

    @Override
    public boolean isTerminal() {
        return this.isTerminal;
    }
}
