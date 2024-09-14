package grapefruit.command.argument.modifier;

import grapefruit.command.util.key.Key;

public interface ContextualModifier<T> extends ArgumentModifier<T> {

    interface Context {

        <T> T require(Key<T> key);

        static Builder builder() {
            return new ModifierImpl.ContextBuilderImpl();
        }

        interface Builder {

            Builder put(String key, Object value);

            Context build();
        }
    }

    interface Factory<T> {

        ContextualModifier<T> createFromContext(Context context);
    }
}
