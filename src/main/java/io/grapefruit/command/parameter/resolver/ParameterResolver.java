package io.grapefruit.command.parameter.resolver;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ParameterResolver<S, T> {

    @NotNull String id();

    @NotNull TypeToken<T> type();

    @NotNull T resolve(final @NotNull S source) throws ParameterResolutionException;

    default @NotNull List<String> listSuggestions(final @NotNull S source, final List<String> args) {
        return List.of();
    }
}
