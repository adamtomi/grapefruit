package grapefruit.command.argument.modifier;

import grapefruit.command.argument.CommandArgumentException;

import java.util.List;
import java.util.function.Function;

public interface ModifierChain<T> {

    T applyChain(T input) throws CommandArgumentException;

    void bake(Function<ModifierPreset, ArgumentModifier<?>> modifierAccess);

    static <T> ModifierChain<T> of(List<ModifierPreset> modifiers) {
        return new ModifierImpl.ModifierChainImpl<>(modifiers);
    }
}
