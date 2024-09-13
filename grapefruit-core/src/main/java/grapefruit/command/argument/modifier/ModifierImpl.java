package grapefruit.command.argument.modifier;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        public T applyChain(T input) throws CommandArgumentException {
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

    static final class ContextImpl implements ContextualModifier.Context {
        private final Registry<Key<?>, Object> valueStore = Registry.create(Registry.DuplicateStrategy.reject());

        public ContextImpl(Map<String, Object> values) {
            Map<Key<?>, Object> transformed = values.entrySet().stream()
                    .collect(toMap(x -> Key.named(x.getValue().getClass(), x.getKey()), Function.identity()));

            this.valueStore.storeEntries(transformed);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T require(Key<T> key) {
            return (T) this.valueStore.get(key)
                    .orElseThrow(() -> new IllegalArgumentException("Unrecognized key: '%s'".formatted(key)));
        }
    }

    record ModifierBlueprintImpl(Key<?> key, @Nullable ContextualModifier.Context context) implements ModifierBlueprint {
        ModifierBlueprintImpl {
            requireNonNull(key, "key cannot be null");
        }
    }
}
