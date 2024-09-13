package grapefruit.command.argument.modifier;

import grapefruit.command.util.key.Key;

import java.util.Map;

public interface ContextualModifier<T> extends ArgumentModifier<T> {

    interface Context {

        <T> T require(Key<T> key);

        static Context of(Map<String, Object> values) {
            return new ModifierImpl.ContextImpl(values);
        }
    }

    interface Factory<T> {

        ContextualModifier<T> createFromContext(Context context);
    }
}
