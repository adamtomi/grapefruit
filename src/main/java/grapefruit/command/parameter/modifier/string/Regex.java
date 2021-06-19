package grapefruit.command.parameter.modifier.string;

import grapefruit.command.parameter.modifier.ParamModifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ParamModifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Regex {

    String value();

    boolean allowUnicode() default false;

    boolean caseInsensitive() default false;
}
