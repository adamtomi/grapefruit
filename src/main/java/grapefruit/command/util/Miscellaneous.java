package grapefruit.command.util;

import grapefruit.command.dispatcher.CommandAuthorizer;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class Miscellaneous {
    public static final Pattern UUID_PATTERN =
            Pattern.compile("([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})");

    private Miscellaneous() {
        throw new UnsupportedOperationException("No instances for you :(");
    }

    public static @Nullable String emptyToNull(final @NotNull String value) {
        return requireNonNull(value, "Value cannot be null").trim().isEmpty() ? null : value;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull TypeToken<T> box(final @NotNull TypeToken<T> type) {
        final Type boxedType = GenericTypeReflector.box(type.getType());
        return (TypeToken<T>) TypeToken.get(boxedType);
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull TypeToken<T> box(final @NotNull Type type) {
        return (TypeToken<T>) TypeToken.get(GenericTypeReflector.box(type));
    }

    public static boolean endsWith(final @NotNull String value, final char suffix) {
        return value.charAt(value.length() - 1) == suffix;
    }

    public static boolean startsWithIgnoreCase(final @NotNull String arg, final @NotNull String prefix) {
        if (arg.length() < prefix.length()) {
            return false;
        }
        return arg.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static boolean isNumber(final @NotNull String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (final NumberFormatException ex) {
            return false;
        }
    }

    public static @NotNull Object nullToPrimitive(final @NotNull Class<?> clazz) {
        if (clazz.equals(Boolean.TYPE)) {
            return Boolean.FALSE;
        } else if (clazz.equals(Byte.TYPE)) {
            return (byte) 0;
        } else if (clazz.equals(Short.TYPE)) {
            return (short) 0;
        } else if (clazz.equals(Integer.TYPE)) {
            return 0;
        } else if (clazz.equals(Float.TYPE)) {
            return 0.0F;
        } else if (clazz.equals(Double.TYPE)) {
            return 0.0D;
        } else if (clazz.equals(Long.TYPE)) {
            return 0L;
        } else if (clazz.equals(Character.TYPE)) {
            return ' ';
        }

        throw new IllegalArgumentException(format("Class %s is not primitive", clazz));
    }

    public static @NotNull String formatFlag(final @NotNull String flagName) {
        return format("--%s", requireNonNull(flagName, "flagName cannot be null"));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static <S> boolean checkAuthorized(final @NotNull S source,
                                              final @Nullable String permission,
                                              final @NotNull CommandAuthorizer<S> authorizer) {
        if (permission == null) {
            return true;
        }

        requireNonNull(source, "source cannot be null");
        requireNonNull(authorizer, "authorizer cannot be null");
        return authorizer.isAuthorized(source, permission);
    }
}
