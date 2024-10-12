package grapefruit.command.compiler.util;

public final class StringUtil {
    private static final String EMPTY_SANITIZED = "__";

    private StringUtil() {}

    /**
     * Picks {@code fallback}, if the value of {@code value} happens to be
     * blank.
     *
     * @param value The value to check
     * @param fallback The fallback value, nullable
     * @return Value, or if it's blank, fallback
     * @see String#isBlank()
     */
    public static String pick(String value, /* @Nullable */ String fallback) {
        return value.isBlank()
                ? fallback
                : value;
    }

    /**
     * Converts {@code camelCase} strings into {@code kebab-case} strings.
     *
     * @param value The value to convert
     * @return The converted value
     */
    public static String toKebabCase(String value) {
        // If we have a blank string, don't do anything.
        if (value.isBlank()) return value;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            // We have an uppercase character, convert it, and include a '-' before it.
            if (Character.isUpperCase(c)) {
                builder.append('-').append(Character.toLowerCase(c));
            } else {
                // It's not uppercase, we can just append it
                builder.append(c);
            }
        }

        return builder.toString();
    }

    /**
     * Gets rid of characters that are not allowed to be in Java
     * identifiers.
     *
     * @param value The original input
     * @return The sanitized string
     */
    public static String sanitize(String value) {
        if (value.isBlank()) return EMPTY_SANITIZED;

        StringBuilder builder = new StringBuilder();
        int first = value.codePointAt(0);

        // If the character is not a valid java identifier start, replace it to '_'
        if (!Character.isJavaIdentifierStart(first)) {
            builder.append('_');
        } else {
            builder.appendCodePoint(first);
        }

        value.codePoints().skip(1L)
                // If it's not a valid java identifier part, replace it to '_'
                .map(c -> Character.isJavaIdentifierPart(c) ? c : '_')
                .forEachOrdered(builder::appendCodePoint);

        String built = builder.toString();

        // "_" is not a valid identifier anymore, but "__" is.
        return built.equals("_") ? EMPTY_SANITIZED : built;
    }
}
