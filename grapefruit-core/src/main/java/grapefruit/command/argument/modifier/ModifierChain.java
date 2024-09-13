package grapefruit.command.argument.modifier;

import grapefruit.command.argument.CommandArgumentException;

import java.util.List;
import java.util.function.Function;

public interface ModifierChain<T> {

    T applyChain(T input) throws CommandArgumentException;

    void bake(Function<ModifierBlueprint, ArgumentModifier<?>> modifierAccess);

    static <T> ModifierChain<T> of(List<ModifierBlueprint> modifiers) {
        return new ModifierImpl.ModifierChainImpl<>(modifiers);
    }
}
