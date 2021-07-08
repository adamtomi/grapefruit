package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Template {

    @NotNull String placeholder();

    @NotNull String replacement();

    static @NotNull Template of(final @NotNull String placeholder, final @Nullable Object replacement) {
        return new TemplateImpl(placeholder, String.valueOf(replacement));
    }
}
