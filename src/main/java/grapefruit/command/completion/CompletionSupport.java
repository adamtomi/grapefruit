package grapefruit.command.completion;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CompletionSupport {
    private CompletionSupport() {}

    public static <T> List<Completion> mapping(final Function<T, Completion> mapper, final Collection<T> elements) {
        return elements.stream().map(mapper).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<Completion> mapping(final Function<T, Completion> mapper, final T... elements) {
        return mapping(mapper, List.of(elements));
    }

    public static <T> List<Completion> strings(final Function<T, String> mapper, final Collection<T> elements) {
        return mapping(Completion::completion, elements.stream().map(mapper).toList());
    }

    @SafeVarargs
    public static <T> List<Completion> strings(final Function<T, String> mapper, final T... elements) {
        return strings(mapper, List.of(elements));
    }

    public static List<Completion> strings(final Collection<String> elements) {
        return strings(Function.identity(), elements);
    }

    public static List<Completion> strings(final String... elements) {
        return strings(List.of(elements));
    }
}
