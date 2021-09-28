package grapefruit.command.util;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AnnotationListTests.DummyAnnotation
public class AnnotationListTests {
    private static final DummyAnnotation ANNOTATION = AnnotationListTests.class.getAnnotation(DummyAnnotation.class);
    
    @Retention(RetentionPolicy.RUNTIME)
    @interface DummyAnnotation {}

    @Test
    public void annotationList_has() {
        final AnnotationList list = new AnnotationList(ANNOTATION);
        assertTrue(list.has(DummyAnnotation.class));
    }

    @Test
    public void annotationList_elements() {
        final AnnotationList list = new AnnotationList(ANNOTATION);
        assertTrue(list.elements().contains(ANNOTATION));
    }

    @Test
    public void annotationList_size() {
        final AnnotationList list = new AnnotationList(ANNOTATION);
        assertEquals(1, list.elements().size());
    }

    @Test
    public void annotationList_find() {
        final AnnotationList list = new AnnotationList(ANNOTATION);
        final Optional<DummyAnnotation> found = list.find(DummyAnnotation.class);
        assertTrue(found.isPresent());
    }

    @Test
    public void annotationList_elementEquals() {
        final AnnotationList list = new AnnotationList(ANNOTATION);
        final Optional<DummyAnnotation> found = list.find(DummyAnnotation.class);
        assertEquals(found.orElseThrow(), ANNOTATION);
    }
}
