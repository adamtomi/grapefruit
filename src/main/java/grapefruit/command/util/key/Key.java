package grapefruit.command.util.key;

import io.leangen.geantyref.TypeToken;

public interface Key<T> {

    TypeToken<T> type();

    String name();

    static <T> Key<T> named(final TypeToken<T> type, final String name) {
        return new KeyImpl<>(type, name);
    }

    static <T> Key<T> named(final Class<T> clazz, final String name) {
        return new KeyImpl<>(TypeToken.get(clazz), name);
    }
}
