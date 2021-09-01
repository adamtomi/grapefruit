package grapefruit.command.util;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public record AnnotationList(@NotNull Collection<Annotation> elements) {

    public AnnotationList(final @NotNull Annotation... annotations) {
        this(List.of(annotations));
    }

    public <A extends Annotation> @NotNull Optional<A> find(final @NotNull Class<A> clazz) {
        requireNonNull(clazz, "clazz cannot be null");
        return elements().stream()
                .filter(x -> x.annotationType().isAssignableFrom(clazz))
                .map(clazz::cast)
                .findFirst();
    }

    public <A extends Annotation> boolean has(final @NotNull Class<A> clazz) {
        return find(clazz).isPresent();
    }

    @Override
    public @NotNull Collection<Annotation> elements() {
        return List.copyOf(this.elements);
    }
}
