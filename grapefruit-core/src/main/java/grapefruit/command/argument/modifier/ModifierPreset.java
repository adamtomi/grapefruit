package grapefruit.command.argument.modifier;

import grapefruit.command.util.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ModifierPreset {

    Key<?> key();

    @Nullable
    ContextualModifier.Context context();

    static ModifierPreset of(Class<?> clazz, Map<String, Object> values) {
        return new ModifierImpl.ModifierPresetImpl(Key.of(clazz), ContextualModifier.Context.of(values));
    }
}
