package io.grapefruit.command.parameter.resolver;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public abstract class AbstractParamterResolver<S, T> implements ParameterResolver<S, T> {
    private final String id;
    private final TypeToken<T> type;

    public AbstractParamterResolver(final @NotNull String id, final @NotNull TypeToken<T> type) {
        this.id = requireNonNull(id, "id cannot be null");
        this.type = requireNonNull(type, "type cannot be null");
    }

    @Override
    public final @NotNull String id() {
        return this.id;
    }

    @Override
    public final @NotNull TypeToken<T> type() {
        return this.type;
    }
}
