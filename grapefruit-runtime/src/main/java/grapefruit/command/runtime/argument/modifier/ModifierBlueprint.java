package grapefruit.command.runtime.argument.modifier;

import grapefruit.command.runtime.util.key.Key;
import org.jetbrains.annotations.Nullable;

public interface ModifierBlueprint {

    Key<?> key();

    @Nullable
    ArgumentModifier.Context context();

    static ModifierBlueprint of(Class<?> clazz, @Nullable ArgumentModifier.Context context) {
        return new ModifierImpl.ModifierBlueprintImpl(Key.of(clazz), context);
    }
}
