package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class TemplateImpl implements Template {
    private final String placeholder;
    private final String replacement;

    TemplateImpl(final @NotNull String placeholder, final @NotNull String replacement) {
        this.placeholder = requireNonNull(placeholder, "placeholder cannot be null");
        this.replacement = requireNonNull(replacement, "replacement cannot be null");
    }

    @Override
    public @NotNull String placeholder() {
        return this.placeholder;
    }

    @Override
    public @NotNull String replacement() {
        return this.replacement;
    }

    @Override
    public @NotNull String toString() {
        return "TemplateImpl[" +
                "placeholder='" + this.placeholder + '\'' +
                ", replacement='" + this.replacement + '\'' +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TemplateImpl template = (TemplateImpl) o;
        return Objects.equals(this.placeholder, template.placeholder)
                && Objects.equals(this.replacement, template.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.placeholder, this.replacement);
    }
}
