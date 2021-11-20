package grapefruit.command.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(Condition.Container.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Condition {

    String[] value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Container {

        Condition[] value();
    }
}
