package io.grapefruit.command.parameter;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

public record CommandParameter(@NotNull TypeToken<?> type,
                               @NotNull Set<Annotation> modifiers) {

    public <A extends Annotation> @NotNull Optional<A> findModifier(final @NotNull Class<A> clazz) {
        return modifiers().stream()
                .filter(x -> x.annotationType().isAssignableFrom(clazz))
                .map(clazz::cast)
                .findFirst();
    }
}
