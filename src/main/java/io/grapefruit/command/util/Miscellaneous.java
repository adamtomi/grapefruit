package io.grapefruit.command.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class Miscellaneous {

    private Miscellaneous() {
        throw new UnsupportedOperationException("No instances for you :(");
    }

    public static @Nullable String emptyToNull(final @NotNull String value) {
        return requireNonNull(value, "Value cannot be null").isEmpty() ? null : value;
    }
}
