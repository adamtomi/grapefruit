package io.grapefruit.command.parameter.resolver;

import io.grapefruit.command.parameter.CommandParameter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ParameterResolver<S, T> {

    @NotNull TypeToken<T> type();

    @NotNull T resolve(final @NotNull S source,
                       final @NotNull List<String> args,
                       final @NotNull CommandParameter param) throws ParameterResolutionException;

    default @NotNull List<String> listSuggestions(final @NotNull S source,
                                                  final @NotNull List<String> args,
                                                  final @NotNull CommandParameter param) {
        return List.of();
    }
}
