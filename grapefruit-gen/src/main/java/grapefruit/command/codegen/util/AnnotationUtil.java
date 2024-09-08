package grapefruit.command.codegen.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.lang.annotation.Annotation;
import java.util.List;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreElements.getAnnotationMirror;
import static java.util.Objects.requireNonNull;

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

    public static <T> T accessAnnotationValue(AnnotationMirror mirror, String name, Class<T> type) {
        AnnotationValue accessor = getAnnotationValue(mirror, name);
        return accessAnnotationValue(accessor, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T accessAnnotationValue(AnnotationValue accessor, Class<T> type) {
        Object value = accessor.getValue();
        if (!type.isInstance(value)) {
            throw new RuntimeException("Annotation value '%s' is not of type '%s'".formatted(
                    value,
                    type.getCanonicalName()
            ));
        }

        return (T) value;
    }

    public static <T> List<T> accessAnnotationValueList(AnnotationMirror mirror, String name, Class<T> type) {
        AnnotationValue accessor = getAnnotationValue(mirror, name);
        return accessor.accept(new ArrayVisitor<>(type), null);
    }

    private static final class ArrayVisitor<T> extends SimpleAnnotationValueVisitor8<List<T>, Void> {
        private final Class<T> type;

        ArrayVisitor(Class<T> type) {
            this.type = requireNonNull(type, "type cannot be null");
        }

        @Override
        public List<T> visitArray(List<? extends AnnotationValue> vals, Void unused) {
            return vals.stream()
                    .map(x -> accessAnnotationValue(x, this.type))
                    .toList();
        }
    }
}
