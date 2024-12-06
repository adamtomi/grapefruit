package grapefruit.command.util;

import java.util.Collection;
import java.util.function.Function;

public final class StringUtil {
    private StringUtil() {
        throw new DontInvokeMe();
    }

    public static <T extends Collection<String>> boolean containsIgnoreCase(final String item, final T items) {
        return items.stream().anyMatch(item::equalsIgnoreCase);
    }

    public static <A, B extends Collection<A>> boolean containsIgnoreCase(final String item, final B items, final Function<A, String> mapper) {
        return items.stream().map(mapper).anyMatch(item::equalsIgnoreCase);
    }

    public static <T extends Collection<String>> boolean containsAnyIgnoreCase(final T a, final T b) {
        for (final String item : a) {
            if (containsIgnoreCase(item, a)) return true;
        }

        return false;
    }

    public static boolean startsWithIgnoreCase(final String arg, final String prefix) {
        if (arg.length() < prefix.length()) return false;

        return arg.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
