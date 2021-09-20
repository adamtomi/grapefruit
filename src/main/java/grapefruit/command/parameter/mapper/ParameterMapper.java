package grapefruit.command.parameter.mapper;

import grapefruit.command.dispatcher.CommandArgument;
import grapefruit.command.parameter.CommandParameter;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;

public interface ParameterMapper<S, T> {

    @NotNull TypeToken<T> type();

    @NotNull T map(final @NotNull S source,
                   final @NotNull Queue<CommandArgument> args,
                   final @NotNull CommandParameter param)
            throws ParameterMappingException;

    default @NotNull List<String> listSuggestions(final @NotNull S source,
                                                  final @NotNull String currentArg,
                                                  final @NotNull CommandParameter param) {
        return List.of();
    }

    default boolean suggestionsNeedValidation() {
        return true;
    }
}
