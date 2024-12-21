package grapefruit.command.testutil;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ExtraAssertions {
    private ExtraAssertions() {}

    public static <T> void assertContainsAll(final Collection<T> result, final Collection<T> expected) {
        assertEquals(expected.size(), result.size());
        for (final T each : expected) {
            assertTrue(result.contains(each));
        }
    }
}