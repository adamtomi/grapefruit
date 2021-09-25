package grapefruit.command.parameter.mapper;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;

public interface ParameterMapper<S, T> {

    @NotNull TypeToken<T> type();

    @NotNull T map(final @NotNull CommandContext<S> context,
                   final @NotNull Queue<CommandInput> args,
                   final @NotNull AnnotationList modifiers)
            throws ParameterMappingException;

    default @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                                  final @NotNull String currentArg,
                                                  final @NotNull AnnotationList modifiers) {
        return List.of();
    }

    default boolean suggestionsNeedValidation() {
        return true;
    }
}
