package grapefruit.command.util;

import java.util.Collection;
import java.util.function.Function;

public final class StringUtil {
    private StringUtil() {}

    public static <T extends Collection<String>> boolean containsIgnoreCase(final String item, final T items) {
        return items.stream().anyMatch(item::equalsIgnoreCase);
    }

    public static <A, B extends Collection<A>> boolean containsIgnoreCase(final String item, final B items, final Function<A, String> mapper) {
        return items.stream().map(mapper).anyMatch(item::equalsIgnoreCase);
    }

    public static boolean startsWithIgnoreCase(final String arg, final String prefix) {
        if (arg.length() < prefix.length()) return false;

        return arg.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static boolean containsWhitespace(final String arg) {
        for (final char c : arg.toCharArray()) {
            if (Character.isWhitespace(c)) return true;
        }

        return false;
    }
}
