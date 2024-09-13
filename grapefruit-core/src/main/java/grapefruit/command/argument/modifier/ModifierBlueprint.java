package grapefruit.command.argument.modifier;

import grapefruit.command.util.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ModifierBlueprint {

    Key<?> key();

    @Nullable
    ContextualModifier.Context context();

    static ModifierBlueprint of(Class<?> clazz, Map<String, Object> values) {
        return new ModifierImpl.ModifierBlueprintImpl(Key.of(clazz), ContextualModifier.Context.of(values));
    }
}
