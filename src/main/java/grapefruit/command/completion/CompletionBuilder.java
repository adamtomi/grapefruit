package grapefruit.command.completion;

import java.util.Collection;
import java.util.function.Function;

public interface CompletionBuilder {

    String input();

    CompletionBuilder include(final CommandCompletion completion);

    CompletionBuilder includeString(final String completion);

    CompletionBuilder include(final Collection<CommandCompletion> completions);

    CompletionBuilder includeStrings(final Collection<String> completions);

    <T> CompletionBuilder include(final Collection<T> completions, final Function<T, CommandCompletion> mapper);

    <T> CompletionBuilder includeStrings(final Collection<T> completions, final Function<T, String> mapper);

    <T> CompletionBuilder include(final T[] completions, final Function<T, CommandCompletion> mapper);

    <T> CompletionBuilder includeStrings(final T[] completions, final Function<T, String> mapper);

    CompletionAccumulator build();

    static CompletionBuilder of(final CompletionFactory factory, final String input) {
        return new CompletionBuilderImpl(factory, input);
    }
}
