package grapefruit.command;

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
}
