package grapefruit.command.util;

import grapefruit.command.dispatcher.CommandAuthorizer;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class Miscellaneous {
    private static final Map<Class<?>, Class<?>> BOX_MAP = Map.of(
            Boolean.TYPE, Boolean.class,
            Byte.TYPE, Byte.class,
            Short.TYPE, Short.class,
            Integer.TYPE, Integer.class,
            Float.TYPE, Float.class,
            Double.TYPE, Double.class,
            Long.TYPE, Long.class,
            Character.TYPE, Character.class
    );

    private Miscellaneous() {
        throw new UnsupportedOperationException("No instances for you :(");
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public static @NotNull Class<?> box(final @NotNull Type type) {
        final @Nullable Class<?> boxed = BOX_MAP.get(type);
        return boxed == null ? (Class<?>) type : boxed;
    }

    public static @Nullable String emptyToNull(final @NotNull String value) {
        return requireNonNull(value, "Value cannot be null").trim().isEmpty() ? null : value;
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
            Double.parseDouble(input);
            return true;
        } catch (final NumberFormatException ex) {
            return false;
        }
    }

    public static @NotNull Object nullToPrimitive(final @NotNull Class<?> clazz) {
        if (clazz.equals(Boolean.TYPE)) {
            return false;
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

    public static @NotNull String formatFlag(final char shorthand) {
        return formatFlag(String.valueOf(shorthand));
    }

    public static @NotNull String formatFlag(final @NotNull String flagName) {
        requireNonNull(flagName, "flagName cannot be null");
        if (flagName.isBlank()) {
            throw new IllegalArgumentException("Cannot format empty string");
        }

        if (flagName.length() == 1) {
            return flagName.startsWith("-") ? flagName : format("-%s", flagName);
        }

        return flagName.startsWith("--") ? flagName : format("--%s", flagName);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static <S> boolean checkAuthorized(final @NotNull S source,
                                              final @Nullable String permission,
                                              final @NotNull CommandAuthorizer<S> authorizer) {
        if (permission == null || permission.isBlank()) {
            return true;
        }

        requireNonNull(source, "source cannot be null");
        requireNonNull(authorizer, "authorizer cannot be null");
        return authorizer.isAuthorized(source, permission);
    }

    public static <T, C extends Collection<T>> @NotNull C mutableCollectionOf(final @NotNull T[] elements,
                                                                              final @NotNull Supplier<C> generator) {
        final C collection = generator.get();
        collection.addAll(Arrays.asList(elements));

        return collection;
    }

    public static boolean containsIgnoreCase(final @NotNull String element,
                                             final @NotNull Collection<String> elements) {
        requireNonNull(element, "element cannot be null");
        requireNonNull(elements, "elements cannot be null");
        return elements.stream().anyMatch(element::equalsIgnoreCase);
    }

    public static @NotNull String parameterName(final @NotNull CommandParameter<?> parameter) {
        return parameter instanceof FlagParameter<?>
                ? ((FlagParameter<?>) parameter).flagName()
                : parameter.name();
    }
}
