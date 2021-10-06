package grapefruit.command.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class AnnotationList {
    private final List<Annotation> elements;

    public AnnotationList(final @NotNull Annotation... annotations) {
        this.elements = List.of(annotations);
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

    public @NotNull Collection<Annotation> elements() {
        return this.elements;
    }

    @Override
    public @NotNull String toString() {
        return "AnnotationList" + this.elements;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AnnotationList that = (AnnotationList) o;
        return Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.elements);
    }
}
