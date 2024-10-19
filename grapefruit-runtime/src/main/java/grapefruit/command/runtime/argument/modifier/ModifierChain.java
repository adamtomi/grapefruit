package grapefruit.command.runtime.argument.modifier;

import grapefruit.command.runtime.argument.CommandArgumentException;

import java.util.List;
import java.util.function.Function;

public interface ModifierChain<T> {

    T apply(T input) throws CommandArgumentException;

    void bake(Function<ModifierBlueprint, ArgumentModifier<?>> modifierAccess);

    static <T> ModifierChain<T> of(List<ModifierBlueprint> modifiers) {
        return new ModifierImpl.ModifierChainImpl<>(modifiers);
    }
}
