package grapefruit.command.runtime.util.key;

import io.leangen.geantyref.TypeToken;

public interface Key<T> {

    TypeToken<T> type();

    interface Named<T> extends Key<T> {

        String name();
    }

    static <T> Key<T> of(Class<T> type) {
        return of(TypeToken.get(type));
    }

    static <T> Key<T> of(TypeToken<T> type) {
        return new Keys.KeyImpl<>(type);
    }

    static <T> Key<T> named(Class<T> type, String name) {
        return named(TypeToken.get(type), name);
    }

    static <T> Key<T> named(TypeToken<T> type, String name) {
        return new Keys.NamedKeyImpl<>(type, name);
    }
}
