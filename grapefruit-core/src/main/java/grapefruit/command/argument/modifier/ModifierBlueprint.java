package grapefruit.command.argument.modifier;

import grapefruit.command.util.key.Key;
import org.jetbrains.annotations.Nullable;

public interface ModifierBlueprint {

    Key<?> key();

    @Nullable
    ContextualModifier.Context context();

    static ModifierBlueprint of(Class<?> clazz, @Nullable ContextualModifier.Context context) {
        return new ModifierImpl.ModifierBlueprintImpl(Key.of(clazz), context);
    }
}
