package io.grapefruit.command.parameter.resolver;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public abstract class AbstractParamterResolver<S, T> implements ParameterResolver<S, T> {
    private final TypeToken<T> type;

    public AbstractParamterResolver(final @NotNull TypeToken<T> type) {
        this.type = requireNonNull(type, "type cannot be null");
    }

    @Override
    public final @NotNull TypeToken<T> type() {
        return this.type;
    }
}
