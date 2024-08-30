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
}
