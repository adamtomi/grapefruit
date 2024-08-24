package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

class BlankCommandInput implements CommandInput {
    private static final String BLANK = " ";
    private final int length;
    private boolean consumed;

    BlankCommandInput(final int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Invalid length! Should be at least 1!");
        }

        this.length = length;
    }

    @Override
    public @NotNull String rawArg() {
        return BLANK.repeat(this.length);
    }

    @Override
    public boolean isConsumed() {
        return this.consumed;
    }

    @Override
    public void markConsumed() {
        this.consumed = true;
    }

    @Override
    public String toString() {
        return "BlankCommandInput[" +
                "length='" + this.length + '\'' +
                ", consumed=" + this.consumed +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BlankCommandInput that = (BlankCommandInput) o;
        return this.consumed == that.consumed && this.length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.length, this.consumed);
    }
}
