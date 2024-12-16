package grapefruit.command.runtime.annotation;

import grapefruit.command.runtime.dispatcher.condition.CommandCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark non-static methods
 * as command handlers.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Command {

    /**
     * The route at which the {@link grapefruit.command.runtime.generated.CommandMirror}
     *  generated based on the method annotated by this annotationwill be registered at.
     */
    String route();

    /**
     * The permission which the user will be required
     * to have in order to execute the command. No
     * permission is required by default.
     */
    String permission() default "";

    /**
     * The command conditions that need to be met before
     * the command execution process can proceed. Empty
     * array by default, meaning that no conditions
     * need to be passed to execute the command.
     */
    Class<? extends CommandCondition>[] conditions() default {};
}
