package grapefruit.command.gen.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreElements.getAnnotationMirror;

public final class AnnotationUtil {
    private AnnotationUtil() {}

    public static AnnotationMirror assertAnnotation(Element element, Class<? extends Annotation> annotation) {
        return getAnnotationMirror(element, annotation)
                .toJavaUtil()
                .orElseThrow(() -> new RuntimeException("Expected element '%s' to be annotated with '%s'".formatted(
                        element.getSimpleName(),
                        annotation.getCanonicalName()
                )));
    }

    @SuppressWarnings("unchecked")
    public static <T> T accessAnnotationValue(AnnotationMirror mirror, String name, Class<T> type) {
        AnnotationValue accessor = getAnnotationValue(mirror, name);
        Object value = accessor.getValue();
        if (!type.isInstance(value)) {
            throw new RuntimeException("Annotation value '%s' is not of type '%s'".formatted(
                    value,
                    type.getCanonicalName()
            ));
        }

        return (T) value;
    }
}
