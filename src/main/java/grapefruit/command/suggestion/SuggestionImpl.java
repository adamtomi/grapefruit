package grapefruit.command.suggestion;

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
}
