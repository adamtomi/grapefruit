package grapefruit.command.suggestion;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class SuggestionImpl implements Suggestion {
    private final String value;

    SuggestionImpl(final String value) {
        this.value = requireNonNull(value, "value cannot be null");
    }

    @Override
    public String stringValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SuggestionImpl that)) return false;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
