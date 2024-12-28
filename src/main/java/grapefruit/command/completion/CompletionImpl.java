package grapefruit.command.completion;

import grapefruit.command.util.ToStringer;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class CompletionImpl implements Comparable<Completion>, Completion {
    static final Completion EMPTY = new CompletionImpl("");
    private final String content;

    CompletionImpl(final String content) {
        this.content = requireNonNull(content, "content cannot be null");
    }

    @Override
    public String content() {
        return this.content;
    }

    @Override
    public String toString() {
        return ToStringer.create(this).append("content", this.content).toString();
    }

    @Override
    public int compareTo(final Completion other) {
        return content().compareTo(other.content());
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final CompletionImpl that = (CompletionImpl) o;
        return Objects.equals(this.content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.content);
    }
}
