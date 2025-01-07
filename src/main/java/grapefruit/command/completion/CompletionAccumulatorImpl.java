package grapefruit.command.completion;

import grapefruit.command.util.ToStringer;

import java.util.List;

import static grapefruit.command.util.StringUtil.startsWithIgnoreCase;
import static java.util.Objects.requireNonNull;

final class CompletionAccumulatorImpl implements CompletionAccumulator {
    private final List<CommandCompletion> completions;
    private final String input;

    CompletionAccumulatorImpl(final List<CommandCompletion> completions, final String input) {
        this.completions = requireNonNull(completions, "completions cannot be null");
        this.input = requireNonNull(input, "input cannot be null");
    }

    @Override
    public List<CommandCompletion> filterCompletions() {
        return this.completions.stream()
                .filter(x -> startsWithIgnoreCase(x.completion(), this.input))
                .toList();
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("input", this.input)
                .append("completions", this.completions)
                .toString();
    }
}
