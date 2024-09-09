package grapefruit.command.codegen.util;

public final class StringUtil {
    private StringUtil() {}

    public static String pick(String value, /* @Nullable */ String fallback) {
        return value.isBlank()
                ? fallback
                : value;
    }
}
