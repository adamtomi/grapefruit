package grapefruit.command.completion;

import grapefruit.command.util.ToStringer;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final CommandCompletionImpl that = (CommandCompletionImpl) o;
        return Objects.equals(this.completion, that.completion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.completion);
    }
}
