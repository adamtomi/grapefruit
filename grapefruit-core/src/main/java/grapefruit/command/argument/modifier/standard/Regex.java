package grapefruit.command.argument.modifier.standard;

import grapefruit.command.argument.modifier.Modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Modifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Regex {

    String value();

    int flags() default 0;
}
