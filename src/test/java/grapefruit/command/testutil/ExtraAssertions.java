package grapefruit.command.testutil;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ExtraAssertions {
    private ExtraAssertions() {}

    public static <T> void assertContainsAll(final Collection<T> expected, final Collection<T> result) {
        assertEquals(expected.size(), result.size(), "Expected size to be %d, got %d".formatted(expected.size(), result.size()));
        for (final T each : expected) {
            assertTrue(result.contains(each), "Expected result to contain element %s".formatted(each));
        }
    }
}
