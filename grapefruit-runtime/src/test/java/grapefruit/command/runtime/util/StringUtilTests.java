package grapefruit.command.runtime.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collection;
import java.util.List;

import static grapefruit.command.runtime.util.StringUtil.containsIgnoreCase;
import static grapefruit.command.runtime.util.StringUtil.startsWithIgnoreCase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilTests {

    @Test
    public void containsIgnoreCase_contains() {
        Collection<String> container = List.of("element", "Item", "HeLLo");
        assertTrue(containsIgnoreCase(container, "hEllO"));
        assertTrue(containsIgnoreCase(container, "ITEM"));
        assertTrue(containsIgnoreCase(container, "ElEmEnT"));
    }

    @Test
    public void containsIgnoreCase_doesNotContain() {
        Collection<String> container = List.of("element", "Item", "HeLLo");
        assertFalse(containsIgnoreCase(container, "H3llo"));
        assertFalse(containsIgnoreCase(container, "ABC"));
        assertFalse(containsIgnoreCase(container, "World"));
        assertFalse(containsIgnoreCase(container, "3l3m3nt"));
        assertFalse(containsIgnoreCase(container, ""));
        assertFalse(containsIgnoreCase(container, " "));
    }

    @ParameterizedTest
    @CsvSource({
            "hello,H",
            "hello world,hE",
            "hello world,HelLo",
            "hello,''",
            "$ThisIsATest,$th",
            "___A,___a",
            "123abc,123ABC",
            "'',''"
    })
    public void startsWithIgnoreCase_startsWith(String item, String prefix) {
        assertTrue(startsWithIgnoreCase(item, prefix));
    }

    @ParameterizedTest
    @CsvSource({
            "Hello,ABC",
            "$$SomeTest,$SomeTest",
            "__a,A__",
            "'',' '"
    })
    public void startsWithIgnoreCase_doesNotStartWith(String item, String prefix) {
        assertFalse(startsWithIgnoreCase(item, prefix));
    }
}
