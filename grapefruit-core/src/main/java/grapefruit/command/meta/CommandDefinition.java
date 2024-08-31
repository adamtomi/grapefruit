package grapefruit.command.meta;

import grapefruit.command.Command;

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
public @interface CommandDefinition {

    /**
     * The route at which the {@link Command} generated
     * based on the method annotated by this annotation
     * will be registered at.
     */
    String route();

    /**
     * The permission which the user will be required
     * to have in order to execute the command. No
     * permission is required by default.
     */
    String permission() default "";
}
