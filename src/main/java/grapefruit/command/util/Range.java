package grapefruit.command.util;

public final class Range {
    private final int from;
    private final int to;

    private Range(final int from, final int to) {
        this.from = from;
        this.to = to;
    }

    public static Range range(final int from, final int to) {
        if (to < from) throw new IllegalArgumentException("to < from");
        return new Range(from, to);
    }

    public int from() {
        return this.from;
    }

    public int to() {
        return this.to;
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("from", this.from)
                .append("to", this.to)
                .toString();
    }
}
