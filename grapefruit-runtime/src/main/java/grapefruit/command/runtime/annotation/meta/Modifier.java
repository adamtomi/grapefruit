package grapefruit.command.runtime.annotation.meta;

import grapefruit.command.runtime.argument.modifier.ArgumentModifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Modifier {

    Class<? extends ArgumentModifier<?>> value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface Factory {

        Class<? extends ArgumentModifier.Factory<?>> value();
    }
}
