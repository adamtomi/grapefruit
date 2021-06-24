package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class CommandArgument {
    private final String rawArg;
    private boolean consumed = false;

    CommandArgument(final @NotNull String rawArg) {
        this.rawArg = requireNonNull(rawArg, "rawInput cannot be null");
    }

    public @NotNull String rawArg() {
        return this.rawArg;
    }

    public boolean isConsumed() {
        return this.consumed;
    }

    public void markConsumed() {
        this.consumed = true;
    }

    @Override
    public String toString() {
        return "CommandInput[" +
                "rawArg='" + this.rawArg + '\'' +
                ", consumed=" + this.consumed +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CommandArgument that = (CommandArgument) o;
        return this.consumed == that.consumed && Objects.equals(this.rawArg, that.rawArg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.rawArg, this.consumed);
    }
}
