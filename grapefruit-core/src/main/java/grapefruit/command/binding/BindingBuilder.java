package grapefruit.command.binding;

import grapefruit.command.argument.mapper.ArgumentMapper;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public interface BindingBuilder<T> {

    void to(ArgumentMapper<T> mapper);

    void toInstance(T t);

    void toSupplier(Supplier<T> supplier);

    interface Annotated<T> extends BindingBuilder<T> {

        <A extends Annotation> BindingBuilder<T> annotatedWith(Class<A> clazz);
    }
}
