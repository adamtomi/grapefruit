package grapefruit.command.gen.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

import static com.google.auto.common.MoreElements.getAnnotationMirror;

public final class GenUtil {
    private GenUtil() {}

    public static AnnotationMirror annotationMirror(Element element, Class<? extends Annotation> annotation) {
        return getAnnotationMirror(element, annotation)
                .toJavaUtil()
                .orElseThrow(() -> new IllegalStateException("Expected element '%s' to be annotated with '%s'".formatted(
                        element.getSimpleName(),
                        annotation.getCanonicalName()
                )));
    }
}
