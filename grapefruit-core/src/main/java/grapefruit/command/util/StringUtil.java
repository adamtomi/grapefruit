package grapefruit.command.util;

import java.util.Collection;

public final class StringUtil {
    private StringUtil() {}

    public static <C extends Collection<String>> boolean containsIgnoreCase(
            C collection,
            String element
    ) {
        return collection.stream().anyMatch(element::equalsIgnoreCase);
    }

    public static boolean startsWithIgnoreCase(String arg, String prefix) {
        /*System.out.println("");
        if (prefix.isEmpty()) {
            System.out.println("prefix is empty, returning true");
            return true;
        }*/
        if (arg.length() < prefix.length()) return false;

        return arg.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
