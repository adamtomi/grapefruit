package grapefruit.command.completion;

import grapefruit.command.util.ToStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class CompletionBuilderImpl implements CompletionBuilder {
    private final List<CommandCompletion> completions = new ArrayList<>();
    private final CompletionFactory factory;
    private final String input;

    CompletionBuilderImpl(final CompletionFactory factory, final String input) {
        this.factory = requireNonNull(factory, "factory cannot be null");
        this.input = requireNonNull(input, "input cannot be null");
    }

    @Override
    public CompletionAccumulator build() {
        return new CompletionAccumulatorImpl(this.completions, this.input);
    }

    @Override
    public String input() {
        return this.input;
    }

    @Override
    public CompletionBuilder include(final CommandCompletion completion) {
        requireNonNull(completion, "completion cannot be null");
        this.completions.add(completion);
        return this;
    }

    @Override
    public CompletionBuilder includeString(final String completion) {
        requireNonNull(completion, "completion cannot be null");
        return include(this.factory.create(completion));
    }

    @Override
    public CompletionBuilder include(final Collection<CommandCompletion> completions) {
        requireNonNull(completions, "completions cannot be null");
        this.completions.addAll(completions);
        return this;
    }

    @Override
    public CompletionBuilder includeStrings(final Collection<String> completions) {
        requireNonNull(completions, "completions cannot be null");
        return includeStrings(completions.stream());
    }

    @Override
    public CompletionBuilder include(final CommandCompletion[] completions) {
        requireNonNull(completions, "completions cannot be null");
        return include(Arrays.asList(completions));
    }

    @Override
    public CompletionBuilder includeStrings(final String[] completions) {
        requireNonNull(completions, "completions cannot be null");
        return includeStrings(Arrays.asList(completions));
    }

    @Override
    public <T> CompletionBuilder include(final Collection<T> completions, final Function<T, CommandCompletion> mapper) {
        requireNonNull(completions, "completions cannot be null");
        requireNonNull(mapper, "mapper cannot be null");
        return include(completions.stream().map(mapper));
    }

    @Override
    public <T> CompletionBuilder includeStrings(final Collection<T> completions, final Function<T, String> mapper) {
        requireNonNull(completions, "completions cannot be null");
        requireNonNull(mapper, "mapper cannot be null");
        return includeStrings(completions.stream().map(mapper));
    }

    @Override
    public <T> CompletionBuilder include(final T[] completions, final Function<T, CommandCompletion> mapper) {
        requireNonNull(completions, "completions cannot be null");
        requireNonNull(mapper, "mapper cannot be null");
        return include(Arrays.stream(completions).map(mapper));
    }

    @Override
    public <T> CompletionBuilder includeStrings(final T[] completions, final Function<T, String> mapper) {
        requireNonNull(completions, "completions cannot be null");
        requireNonNull(mapper, "mapper cannot be null");
        return includeStrings(Arrays.stream(completions).map(mapper));
    }

    private CompletionBuilder includeStrings(final Stream<String> completions) {
        return include(completions.map(this.factory::create));
    }

    private CompletionBuilder include(final Stream<CommandCompletion> completions) {
        this.completions.addAll(completions.toList());
        return this;
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("input", this.input)
                .append("completions", this.completions)
                .toString();
    }
}
