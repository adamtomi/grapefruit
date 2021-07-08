package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ParsedCommandArgument {
    private final String name;
    private @Nullable Object parsedValue;

    ParsedCommandArgument(final @NotNull String name) {
        this.name = requireNonNull(name, "name cannot be null");
    }

    public @NotNull String name() {
        return this.name;
    }

    public @NotNull Optional<Object> parsedValue() {
        return Optional.ofNullable(this.parsedValue);
    }

    public void parsedValue(final @Nullable Object value) {
        this.parsedValue = value;
    }

    @Override
    public String toString() {
        return "ParsedCommandInput[" +
                "name='" + this.name + '\'' +
                ", parsedValue=" + this.parsedValue +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ParsedCommandArgument that = (ParsedCommandArgument) o;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
