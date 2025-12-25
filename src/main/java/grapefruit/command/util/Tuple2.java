package grapefruit.command.util;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class Tuple2<L, R> {
    private final @Nullable L left;
    private final @Nullable R right;

    public Tuple2(final @Nullable L left, final @Nullable R right) {
        this.left = left;
        this.right = right;
    }

    public Optional<L> left() {
        return Optional.ofNullable(this.left);
    }

    public Optional<R> right() {
        return Optional.ofNullable(this.right);
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("left", this.left)
                .append("right", this.right)
                .toString();
    }
}
