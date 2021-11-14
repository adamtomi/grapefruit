package grapefruit.command.util;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.parameter.PresenceFlagParameter;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.mapper.AbstractParameterMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MiscellaneousTests {

    @ParameterizedTest
    @ValueSource(classes = {int.class, boolean.class, long.class})
    public void box_primitiveInput(final Class<?> clazz) {
        final Class<?> boxed = Miscellaneous.box(clazz);
        assertNotEquals(clazz, boxed);
    }

    @ParameterizedTest
    @ValueSource(classes = {Object.class, String.class, CharSequence.class})
    public void box_objectInput(final Class<?> clazz) {
        final Class<?> boxed = Miscellaneous.box(clazz);
        assertEquals(clazz, boxed);
    }

    @ParameterizedTest
    @ValueSource(classes = {int.class, boolean.class, long.class})
    public void box_typeToken_primitiveInput(final Class<?> clazz) {
        final TypeToken<?> type = TypeToken.of(clazz);
        final TypeToken<?> boxed = Miscellaneous.box(type);
        assertNotEquals(type, boxed);
    }

    @ParameterizedTest
    @ValueSource(classes = {Object.class, String.class, CharSequence.class})
    public void box_typeToken_objectInput(final Class<?> clazz) {
        final TypeToken<?> type = TypeToken.of(clazz);
        final TypeToken<?> boxed = Miscellaneous.box(type);
        assertEquals(type, boxed);
    }

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
    @CsvSource({"first,--first", "--second,--second", "-,-", "a,-a", "b,-b"})
    public void formatFlag_validInput(final String input, final String flagName) {
        assertEquals(Miscellaneous.formatFlag(input), flagName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    public void formatFlag_invalidInput(final String input) {
        assertThrows(IllegalArgumentException.class, () -> Miscellaneous.formatFlag(input));
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

    @ParameterizedTest
    @CsvSource({"first,true", "fifth,false", "second,true", "FOuRth,true", "something,false"})
    public void containsIgnoreCase_validInput(final String element, final boolean shouldContain) {
        final Collection<String> strings = Set.of("first", "SECOND", "thIrD", "FouRtH");
        final boolean contains = Miscellaneous.containsIgnoreCase(element, strings);
        assertEquals(contains, shouldContain);
    }

    @ParameterizedTest
    @ValueSource(strings = {"parameter", "param", "name", "_my-name"})
    public void parameterName_standardParameter(final String name) {
        final CommandParameter<Object> param = new DummyParameter(name);
        final String result = Miscellaneous.parameterName(param);
        assertEquals(name, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"flag", "other-flag", "_this-is-a-flag-too"})
    public void parameterName_flagParameter(final String name) {
        final FlagParameter<Object> param = new PresenceFlagParameter<>(name, ' ', "__dummy", new AnnotationList());
        final String result = Miscellaneous.parameterName(param);
        assertEquals(name, result);
    }

    @ParameterizedTest
    @ValueSource(classes = {String.class, Object.class, Integer.class, Boolean.class})
    public void primitiveSafeValue_nonPrimitive(final Class<?> clazz) {
        final Object result = Miscellaneous.primitiveSafeValue(new DummyParameter(clazz, "test"), null);
        assertNull(result);
    }

    @ParameterizedTest
    @ValueSource(classes = {char.class, boolean.class, int.class, long.class, float.class})
    public void primitiveSafeValue_primitive(final Class<?> clazz) {
        final Object result = Miscellaneous.primitiveSafeValue(new DummyParameter(clazz, "test"), null);
        assertNotNull(result);
    }

    private static final class DummyParameter extends StandardParameter<Object> {
        private DummyParameter(final String name) {
            this(Object.class, name);
        }

        private DummyParameter(final Class<?> clazz,
                               final String name) {
            super(name, false, TypeToken.of(clazz), new AnnotationList(), new DummyParameterMapper());
        }
    }

    private static final class DummyParameterMapper extends AbstractParameterMapper<Object, String> {
        private DummyParameterMapper() {
            super(TypeToken.of(String.class));
        }

        @Override
        public String map(final CommandContext<Object> context,
                          final Queue<CommandInput> args,
                          final AnnotationList modifiers) {
            return "Hello there!";
        }
    }
}
