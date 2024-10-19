package grapefruit.command.annotation.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates, that the value for the annotated method parameter should
 * be mapped using the {@link grapefruit.command.argument.mapper.ArgumentMapper} with the specified name.
 * <p>
 * It is possible to annotate other annotation types with {@link MappedBy}.
 * <pre>{@code
 * @MappedBy("__MY_ANNOTATION__")
 * @Retention(RetentionPolicy.SOURCE)
 * @Target(ElementType.PARAMETER)
 * public @interface MyAnnotation {}
 * }</pre>
 *
 * This way, {@code @MyAnnotation} becomes an alias of {@code @MappedBy("__MY_ANNOTATION__")}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
public @interface MappedBy {

    /**
     * The name of the {@link grapefruit.command.argument.mapper.ArgumentMapper}
     * to use.
     */
    String value();
}
