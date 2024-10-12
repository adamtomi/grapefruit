package grapefruit.command.annotation.modifier.string;

import grapefruit.command.annotation.modifier.Modifier;
import grapefruit.command.argument.modifier.builtin.RegexModifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Modifier.Factory(value = RegexModifier.Factory.class)
public @interface Regex {

    String value();

    int flags() default 0;
}
