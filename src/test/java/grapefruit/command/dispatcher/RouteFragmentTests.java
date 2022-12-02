package grapefruit.command.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteFragmentTests {

    @ParameterizedTest
    @CsvSource({"test,1", "hello there,2", "this is just a command route,6"})
    public void routeFragment_parse_length(final String route, final int length) {
        final List<RouteFragment> result = RouteFragment.parseRoute(route);
        assertEquals(result.size(), length);
    }

    @Test
    public void routeFragment_parse_aliass() {
        final String[] parts = { "first", "second", "third", "fourth", "hello", "there" };
        final List<RouteFragment> route = RouteFragment.parseRoute(String.join("|", parts));
        assertEquals(route.size(), 1);
        final RouteFragment fragment = route.get(0);
        assertEquals(fragment.primary(), parts[0]);

        for (int i = 1; i < parts.length; i++) {
            assertEquals(parts[i], fragment.aliases().get(i - 1)); // Subtract one as the original array contained the primary alias as well
        }
    }
}
