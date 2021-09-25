package grapefruit.command.parameter.mapper;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public abstract class AbstractParameterMapper<S, T> implements ParameterMapper<S, T> {
    private final TypeToken<T> type;

    public AbstractParameterMapper(final @NotNull TypeToken<T> type) {
        this.type = requireNonNull(type, "type cannot be null");
    }

    @Override
    public final @NotNull TypeToken<T> type() {
        return this.type;
    }
}
