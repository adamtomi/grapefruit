package grapefruit.command.runtime.argument.modifier;

import grapefruit.command.runtime.argument.CommandArgumentException;
import grapefruit.command.runtime.util.Registry;
import grapefruit.command.runtime.util.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

final class ModifierImpl {

    static final class ModifierChainImpl<T> implements ModifierChain<T> {
        private final List<ArgumentModifier<T>> bakedModifiers = new ArrayList<>();
        private final List<ModifierBlueprint> modifiers;

        ModifierChainImpl(List<ModifierBlueprint> modifiers) {
            this.modifiers = requireNonNull(modifiers, "modifiers cannot be null");
        }

        @Override
        public T apply(T input) throws CommandArgumentException {
            T result = input;
            for (ArgumentModifier<T> bakedModifier : this.bakedModifiers) result = bakedModifier.apply(result);
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void bake(Function<ModifierBlueprint, ArgumentModifier<?>> modifierAccess) {
            if (!this.bakedModifiers.isEmpty()) return;
            for (ModifierBlueprint modifier : this.modifiers) this.bakedModifiers.add((ArgumentModifier<T>) modifierAccess.apply(modifier));
        }
    }

    static final class ContextImpl implements ArgumentModifier.Context {
        private final Registry<Key<?>, Object> valueStore = Registry.create(Registry.DuplicateStrategy.reject());

        ContextImpl(Map<String, Object> values) {
            Map<Key<?>, Object> transformed = values.entrySet().stream()
                    .collect(toMap(x -> Key.named(x.getValue().getClass(), x.getKey()), Map.Entry::getValue));

            this.valueStore.storeEntries(transformed);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T require(Key<T> key) {
            return (T) this.valueStore.get(key)
                    .orElseThrow(() -> new IllegalArgumentException("Unrecognized key: '%s'".formatted(key)));
        }
    }

    static final class ContextBuilderImpl implements ArgumentModifier.Context.Builder {
        private final Map<String, Object> values = new HashMap<>();

        ContextBuilderImpl() {}

        @Override
        public ArgumentModifier.Context.Builder put(String key, Object value) {
            this.values.put(
                    requireNonNull(key, "key cannot be null"),
                    requireNonNull(value, "value cannot be null")
            );
            return this;
        }

        @Override
        public ArgumentModifier.Context build() {
            return new ContextImpl(this.values);
        }
    }

    record ModifierBlueprintImpl(Key<?> key, @Nullable ArgumentModifier.Context context) implements ModifierBlueprint {
        ModifierBlueprintImpl {
            requireNonNull(key, "key cannot be null");
        }
    }
}
