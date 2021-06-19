package io.grapefruit.command.util;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

public final class Miscellaneous {

    private Miscellaneous() {
        throw new UnsupportedOperationException("No instances for you :(");
    }

    public static @Nullable String emptyToNull(final @NotNull String value) {
        return requireNonNull(value, "Value cannot be null").isEmpty() ? null : value;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull TypeToken<T> box(final @NotNull TypeToken<T> type) {
        final Type boxedType = GenericTypeReflector.box(type.getType());
        return (TypeToken<T>) TypeToken.get(boxedType);
    }
}
