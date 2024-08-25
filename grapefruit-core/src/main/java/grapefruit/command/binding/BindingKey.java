package grapefruit.command.binding;

import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.util.Objects;

public class BindingKey<T> {
    private final TypeToken<T> type;
    private final Class<? extends Annotation> annotation;

    private BindingKey(TypeToken<T> type, Class<? extends Annotation> annotation) {
        this.type = type;
        this.annotation = annotation;
    }

    public static <T, A extends Annotation> BindingKey<T> of(TypeToken<T> type, Class<A> annotation) {
        return new BindingKey<>(type, annotation);
    }

    public static <T, A extends Annotation> BindingKey<T> of(Class<T> type, Class<A> annotation) {
        return of(TypeToken.of(type), annotation);
    }

    public static <T> BindingKey<T> of(TypeToken<T> type) {
        return of(type, null);
    }

    public static <T> BindingKey<T> of(Class<T> type) {
        return of(TypeToken.of(type));
    }

    public TypeToken<T> type() {
        return this.type;
    }

    public Class<? extends Annotation> annotation() {
        return this.annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BindingKey<?> that = (BindingKey<?>) o;
        return Objects.equals(this.type, that.type) && Objects.equals(this.annotation, that.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.annotation);
    }
}