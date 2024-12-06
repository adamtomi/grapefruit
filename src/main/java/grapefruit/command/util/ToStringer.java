package grapefruit.command.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class ToStringer {
    private final Class<?> clazz;
    private final Map<String, Object> properties;

    private ToStringer(final Class<?> clazz, final Map<String, Object> properties) {
        this.clazz = requireNonNull(clazz, "clazz cannot be null");
        this.properties = requireNonNull(properties, "properties cannot be null");
    }

    private static String className(final Class<?> clazz) {
        final String[] path = clazz.getName().split("\\.");
        return path[path.length - 1];
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder()
                .append(className(this.clazz))
                .append("(");

        for (final Iterator<Map.Entry<String, Object>> iter = this.properties.entrySet().iterator(); iter.hasNext(); ) {
            final Map.Entry<String, Object> entry = iter.next();
            builder.append(entry.getKey()).append("=").append(entry.getValue());

            if (iter.hasNext()) builder.append(", ");
        }

        return builder.append(")").toString();
    }

    public static Builder create(final Object obj) {
        return create(obj.getClass());
    }

    public static Builder create(final Class<?> clazz) {
        return new Builder(clazz);
    }

    public static final class Builder {
        private final Map<String, Object> properties = new HashMap<>();
        private final Class<?> clazz;

        private Builder(final Class<?> clazz) {
            this.clazz = requireNonNull(clazz, "clazz cannot be null");
        }

        public Builder append(final String property, final Object value) {
            this.properties.put(requireNonNull(property, "property cannot be null"), value);
            return this;
        }

        @Override
        public String toString() {
            return new ToStringer(this.clazz, this.properties).toString();
        }
    }
}
