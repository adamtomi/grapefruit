package grapefruit.command.compiler.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static grapefruit.command.compiler.util.StringUtil.pick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilTests {

    @Test
    public void pick_fallback() {
        assertNull(pick("", null));
        assertNull(pick(" ", null));
    }

    @Test
    public void pick_argument() {
        String argument = "hello";
        assertEquals(argument, pick(argument, null));
        assertEquals(argument, pick(argument, ""));
        assertEquals(argument, pick(argument, " "));
    }

    @ParameterizedTest
    @CsvSource({
            "hello,hello",
            "helloThere,hello-there",
            "someLongStringWithCamelCaseFormatting,some-long-string-with-camel-case-formatting",
            "'',''",
            "' ',' '"
    })
    public void toKebabCase_tests(String input, String expected) {
        assertEquals(expected, StringUtil.toKebabCase(input));
    }

    @ParameterizedTest
    @CsvSource({
            "abc,abc",
            "hello_there,hello_there",
            "helloThere,helloThere",
            "hello-there,hello_there",
            "a-b-c,a_b_c",
            "-,__",
            "$-b-c,$_b_c",
            "§§some|string,__some_string",
            "-1-2_a,_1_2_a",
            "123-abc,_23_abc"
    })
    public void sanitize_tests(String input, String expected) {
        assertEquals(expected, StringUtil.sanitize(input));
    }
}
