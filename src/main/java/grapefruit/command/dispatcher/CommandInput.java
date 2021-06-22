package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class CommandInput {
    private final String rawInput;
    private boolean consumed = false;

    CommandInput(final @NotNull String rawInput) {
        this.rawInput = requireNonNull(rawInput, "rawInput cannot be null");
    }

    public @NotNull String rawInput() {
        return this.rawInput;
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
                "rawInput='" + this.rawInput + '\'' +
                ", consumed=" + this.consumed +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CommandInput that = (CommandInput) o;
        return this.consumed == that.consumed && Objects.equals(this.rawInput, that.rawInput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.rawInput, this.consumed);
    }
}
