package grapefruit.command.annotation.inject;

import grapefruit.command.dispatcher.CommandContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter annotated with {@link grapefruit.command.annotation.CommandDefinition}
 * as a non-argument parameter. That means that a value for this parameter should not be obtained
 * from user input.
 * Values for such kind of parameters are to be included in the context upon the context's creation,
 * before it is passed to {@link grapefruit.command.dispatcher.CommandDispatcher#dispatch(CommandContext, String)}.
 * The key to retrieve the supplied value is created from the parameter's type and the name provided by
 * {@link InjectedBy#value()}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
public @interface InjectedBy {

    /**
     * The name of the key to be used to retrieve the value.
     */
    String value();

    /**
     * Whether {@code null} should be acceptable. {@code true} by default.
     */
    boolean nullable() default true;
}
