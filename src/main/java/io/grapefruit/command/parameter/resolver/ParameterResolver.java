package io.grapefruit.command.parameter.resolver;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface ParameterResolver<S, T> {

    @NotNull T resolve(final @NotNull S source) throws ParameterResolutionException;

    default @NotNull List<String> listSuggestions(final @NotNull S source, final List<String> args) {
        return List.of();
    }
}
