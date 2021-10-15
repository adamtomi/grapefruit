package grapefruit.command.dispatcher;

import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface SuggestionProvider<S> {

    @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                          final @NotNull String currentArg,
                                          final @NotNull AnnotationList modifiers);
}
