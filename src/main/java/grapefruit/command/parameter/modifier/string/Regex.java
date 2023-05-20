package grapefruit.command.parameter.modifier.string;

import grapefruit.command.parameter.modifier.Modifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test the parameter against the provided regex. Only works
 * with {@link java.lang.String} parameters.
 */
@Modifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Regex {

    String value();

    /**
     * @deprecated Use {@link this#flags()} instead. To enable
     * unicode characters, the {@link java.util.regex.Pattern#UNICODE_CHARACTER_CLASS}
     * flag may be used. Example:
     *
     * <p>@Regex(value = "\\w+", flags = Pattern.UNICODE_CHARACTER_CLASS)</p>
     */
    @Deprecated(forRemoval = true)
    boolean allowUnicode() default false;

    @Deprecated(forRemoval = true)
    boolean caseInsensitive() default false;

    /**
     * Set Pattern object flags.
     *
     * @see java.util.regex.Pattern
     */
    int flags() default 0;
}
