package grapefruit.command.completion;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public interface CompletionBuilder {

    CompletionBuilder add(final String completion);

    CompletionBuilder add(final String... completions);

    CompletionBuilder add(final Iterable<String> completions);

    <T, C extends Collection<T>> CompletionBuilder add(final Function<T, String> mapper, final C collection);

    <T> CompletionBuilder add(final Function<T, String> mapper, final T[] array);

    // TODO add methods to include Completion objects

    CommandCompletion build();

    // TODO Collector support

    Collector<String, List<Completion>, CommandCompletion> collectStrings();

    Collector<Completion, List<Completion>, CommandCompletion> collectCompletions();
}
