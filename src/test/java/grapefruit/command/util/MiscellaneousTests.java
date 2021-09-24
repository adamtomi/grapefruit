package grapefruit.command.util;

import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.AnnotatedType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MiscellaneousTests {

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    public void emptyToNull_validInput(final String input) {
        assertNotNull(Miscellaneous.emptyToNull(input));
    }

    @Test
    public void emptyToNull_invalidInput() {
        assertNull(Miscellaneous.emptyToNull(""));
    }

    @Test
    public void typeToken_validInput() {
        final TypeToken<List<String>> listType = new TypeToken<>() {};
        final AnnotatedType type = listType.getAnnotatedType();
        assertEquals(listType, Miscellaneous.constructTypeToken(type));
    }

    @Test
    public void endsWith_validInput() {
        assertTrue(Miscellaneous.endsWith("first", 't'));
    }

    @Test
    public void endsWith_invalidInput() {
        assertFalse(Miscellaneous.endsWith("param", 'c'));
    }

    @ParameterizedTest
    @CsvSource({"first,''", "second,second", "third,thi"})
    public void startsWithIgnoreCase_validInput(final String param, final String prefix) {
        assertTrue(Miscellaneous.startsWithIgnoreCase(param, prefix));
    }

    @ParameterizedTest
    @CsvSource({"first,notfirst", "'',second", "third,d"})
    public void startsWithIgnoreCase_invalidInput(final String param, final String prefix) {
        assertFalse(Miscellaneous.startsWithIgnoreCase(param, prefix));
    }

    @ParameterizedTest
    @ValueSource(strings = {"5.3", "1", "0", "-1", "04", "+5"})
    public void isNumber_validInput(final String number) {
        assertTrue(Miscellaneous.isNumber(number));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second", "three", "minus five and a half"})
    public void isNumber_invalidInput(final String number) {
        assertFalse(Miscellaneous.isNumber(number));
    }

    @Test
    public void nullToPrimitive_booleanInput() {
        assertEquals(Miscellaneous.nullToPrimitive(Boolean.TYPE), false);
    }

    @Test
    public void nullToPrimitive_charInput() {
        assertEquals(Miscellaneous.nullToPrimitive(Character.TYPE), ' ');
    }

    @Test
    public void nullToPrimitive_byteInput() {
        assertEquals(Miscellaneous.nullToPrimitive(Byte.TYPE), (byte)0);
    }

    @Test
    public void nullToPrimitive_shortInput() {
        assertEquals(Miscellaneous.nullToPrimitive(Short.TYPE), (short) 0);
    }

    @Test
    public void nullToPrimitive_intInput() {
        assertEquals(Miscellaneous.nullToPrimitive(Integer.TYPE), 0);
    }

    @Test
    public void nullToPrimitive_floatInput() {
        assertEquals(Miscellaneous.nullToPrimitive(Float.TYPE), 0.0F);
    }

    @Test
    public void nullToPrimitve_doubleInput() {
        assertEquals(Miscellaneous.nullToPrimitive(Double.TYPE), 0D);
    }

    @Test
    public void nullToPrimitive_longInput() {
        assertEquals(Miscellaneous.nullToPrimitive(Long.TYPE), 0L);
    }

    @ParameterizedTest
    @ValueSource(classes = {Object.class, Miscellaneous.class})
    public void nullToPrimitive_invalidInput(final Class<?> clazz) {
        assertThrows(IllegalArgumentException.class, () -> Miscellaneous.nullToPrimitive(clazz));
    }

    @ParameterizedTest
    @CsvSource({"first,--first", "--second,--second", "'',--"})
    public void formatFlag_validInput(final String input, final String flagName) {
        assertEquals(Miscellaneous.formatFlag(input), flagName);
    }

    @ParameterizedTest
    @CsvSource({"some.permission,true", "no.permission,false", "' ',true", "'',true", ",true"})
    public void checkAuthorized_validInput(final String permissionString, final boolean hasPermission) {
        final CommandSource source = new CommandSource();
        assertEquals(Miscellaneous.checkAuthorized(source, permissionString, CommandSource::hasPermission), hasPermission);
    }

    private static final class CommandSource {
        private final Set<String> permissions = Set.of("some.permission", "other.permission");

        boolean hasPermission(final String permission) {
            return this.permissions.contains(permission);
        }
    }

    @Test
    public void mutableCollectionOf_validInput() {
        final String[] elements = {"first", "second", "third", "random john"};
        final Set<String> mutableCopy = Miscellaneous.mutableCollectionOf(elements, HashSet::new);
        assertDoesNotThrow(() -> mutableCopy.add("Hello there"));
    }

    @Test
    public void mutableCollectionOf_invalidInput() {
        final String[] elements = {"first", "second", "third", "random john"};
        assertThrows(UnsupportedOperationException.class, () -> Miscellaneous.mutableCollectionOf(elements, Set::of));
    }
}
