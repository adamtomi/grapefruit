package grapefruit.command.runtime.annotation.meta;

import grapefruit.command.runtime.annotation.Command;
import grapefruit.command.runtime.dispatcher.CommandContext;
import grapefruit.command.runtime.dispatcher.CommandDispatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter annotated with {@link Command}
 * as a non-argument parameter. That means that a value for this parameter should not be obtained
 * from user input.
 * Values for such kind of parameters are to be included in the context upon the context's creation,
 * before it is passed to {@link CommandDispatcher#dispatch(CommandContext, String)}.
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
