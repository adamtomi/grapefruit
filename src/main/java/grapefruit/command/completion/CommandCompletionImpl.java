package grapefruit.command.completion;

import grapefruit.command.util.ToStringer;

import static java.util.Objects.requireNonNull;

final class CommandCompletionImpl implements CommandCompletion {
    private final String completion;
    
    CommandCompletionImpl(final String completion) {
        this.completion = requireNonNull(completion, "completion cannot be null");
    }

    @Override
    public String completion() {
        return this.completion;
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("completion", this.completion)
                .toString();
    }
}
