package grapefruit.command.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilTests {

    record Item(String name) {}

    @Test
    public void containsIgnoreCase_doesContain() {
        final List<String> items = List.of("Hello", "wOrLd");
        assertTrue(StringUtil.containsIgnoreCase("hello", items));
        assertTrue(StringUtil.containsIgnoreCase("hElLO", items));
        assertTrue(StringUtil.containsIgnoreCase("world", items));
        assertTrue(StringUtil.containsIgnoreCase("wORLD", items));
    }

    @Test
    public void containsIgnoreCase_doesNotContain() {
        final List<String> items = List.of("Hello", "wOrLd");
        assertFalse(StringUtil.containsIgnoreCase("test", items));
        assertFalse(StringUtil.containsIgnoreCase("another", items));
        assertFalse(StringUtil.containsIgnoreCase("hi", items));
        assertFalse(StringUtil.containsIgnoreCase("command", items));
    }

    @Test
    public void containsIgnoreCase_withMapper_doesContain() {
        final List<Item> items = List.of(new Item("Hello"), new Item("wOrLd"));
        assertTrue(StringUtil.containsIgnoreCase("hello", items, Item::name));
        assertTrue(StringUtil.containsIgnoreCase("hElLO", items, Item::name));
        assertTrue(StringUtil.containsIgnoreCase("world", items, Item::name));
        assertTrue(StringUtil.containsIgnoreCase("wORLD", items, Item::name));
    }

    @Test
    public void containsIgnoreCase_withMapper_doesNotContain() {
        final List<Item> items = List.of(new Item("Hello"), new Item("wOrLd"));
        assertFalse(StringUtil.containsIgnoreCase("test", items, Item::name));
        assertFalse(StringUtil.containsIgnoreCase("another", items, Item::name));
        assertFalse(StringUtil.containsIgnoreCase("hi", items, Item::name));
        assertFalse(StringUtil.containsIgnoreCase("command", items, Item::name));
    }

    @ParameterizedTest
    @CsvSource({
            "hello,He",
            "WoRld,wO",
            "TEsT,te"
    })
    public void startsWithIgnoreCase_doesStartWith(final String arg, final String prefix) {
        assertTrue(StringUtil.startsWithIgnoreCase(arg, prefix));
    }

    @ParameterizedTest
    @CsvSource({
            "hello,hI",
            "WoRld,COmmand",
            "TEsT,HI"
    })
    public void startsWithIgnoreCase_doesNotStartWith(final String arg, final String prefix) {
        assertFalse(StringUtil.startsWithIgnoreCase(arg, prefix));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Hello World",
            "A B",
            "test another",
            "  a",
            " "
    })
    public void containsWhitespace_doesContain(final String arg) {
        assertTrue(StringUtil.containsWhitespace(arg));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Hello",
            "World",
            "TEST"
    })
    public void containsWhitespace_doesNotContain(final String arg) {
        assertFalse(StringUtil.containsWhitespace(arg));
    }
}
