package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.MethodParameterParser.RuleViolationException;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.PresenceFlagParameter;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.ValueFlagParameter;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMapperRegistry;
import grapefruit.command.parameter.mapper.ParameterMappingException;
import grapefruit.command.parameter.modifier.Flag;
import grapefruit.command.parameter.modifier.Mapper;
import grapefruit.command.parameter.modifier.OptParam;
import grapefruit.command.parameter.modifier.Range;
import grapefruit.command.parameter.modifier.Source;
import grapefruit.command.parameter.modifier.string.Greedy;
import grapefruit.command.parameter.modifier.string.Quotable;
import grapefruit.command.parameter.modifier.string.Regex;
import grapefruit.command.util.AnnotationList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MethodParameterParserTests {
    private Method violatesGreedyAndQuotable;
    private Method violatesGreedyQuotableRegexString;
    private Method violatesGreedyMustBeLast;
    private Method violatesRangeNotANumber;
    private Method violatesSourceHasMoreAnnotations;
    private Method violatesSourceMustBeFirst;
    private Method violatesUnrecognizedAnnotation;
    private Method violatesFlagCannotBeOptional;
    private Method methodWithParameterOfUnrecognizedType;
    private Method methodWithParameterWithUnrecognizedName;
    private Method methodWithAmbigiousFlags;
    private Method method01;
    private Method method02;
    private Method method03;
    private Method method04;

    @BeforeAll
    public void setUp() throws ReflectiveOperationException {
        final Class<MethodContainer> clazz = MethodContainer.class;
        this.violatesGreedyAndQuotable = clazz.getDeclaredMethod("violatesGreedyAndQuotable", String.class);
        this.violatesGreedyQuotableRegexString = clazz.getDeclaredMethod("violatesGreedyQuotableRegexNotString", int.class);
        this.violatesGreedyMustBeLast = clazz.getDeclaredMethod("violatesGreedyMustBeLast", String.class, int.class);
        this.violatesRangeNotANumber = clazz.getDeclaredMethod("violatesRangeMustBeANumber", boolean.class);
        this.violatesSourceHasMoreAnnotations = clazz.getDeclaredMethod("violatesSourceHasMoreAnnotations", String.class);
        this.violatesSourceMustBeFirst = clazz.getDeclaredMethod("violatesSourceNthParameter", String.class, int.class, Object.class);
        this.violatesUnrecognizedAnnotation = clazz.getDeclaredMethod("violatesUnrecognizedAnnotation", String.class);
        this.violatesFlagCannotBeOptional = clazz.getDeclaredMethod("violatesFlagMustNotBeOptional", int.class);
        this.methodWithParameterOfUnrecognizedType = clazz.getDeclaredMethod("methodWithParameterOfUnrecognizedType", Object.class);
        this.methodWithParameterWithUnrecognizedName = clazz.getDeclaredMethod("methodWithParameterWithUnrecognizedName", Object.class);
        this.methodWithAmbigiousFlags = clazz.getDeclaredMethod("methodWithAmbigiousFlags", String.class, boolean.class);
        this.method01 = clazz.getDeclaredMethod("method01", String.class, int.class, short.class);
        this.method02 = clazz.getDeclaredMethod("method02", Object.class, boolean.class, long.class);
        this.method03 = clazz.getDeclaredMethod("method03",
                Object.class, boolean.class, byte.class, short.class, int.class, float.class, double.class, char.class);
        this.method04 = clazz.getDeclaredMethod("method04", List.class, Set.class, Collection.class);
    }

    @Test
    public void collectParameters_rule_greedyAndQuotable() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(RuleViolationException.class, () -> parser.collectParameters(this.violatesGreedyAndQuotable));
    }

    @Test
    public void collectParameters_rule_greedyAQuotableRegexInvalidType() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(RuleViolationException.class, () -> parser.collectParameters(this.violatesGreedyQuotableRegexString));
    }

    @Test
    public void collectParameters_rule_greedyMustBeLast() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(RuleViolationException.class, () -> parser.collectParameters(this.violatesGreedyMustBeLast));
    }

    @Test
    public void collectParameters_rule_rangeInvalidType() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(RuleViolationException.class, () -> parser.collectParameters(this.violatesRangeNotANumber));
    }

    @Test
    public void collectParameters_rule_sourceHasMoreAnnotations() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(RuleViolationException.class, () -> parser.collectParameters(this.violatesSourceHasMoreAnnotations));
    }

    @Test
    public void collectParameters_rule_sourceNthParameter() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(RuleViolationException.class, () -> parser.collectParameters(this.violatesSourceMustBeFirst));
    }

    @Test
    public void collectParameters_rule_unrecognizedAnnotation() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(RuleViolationException.class, () -> parser.collectParameters(this.violatesUnrecognizedAnnotation));
    }

    @Test
    public void collectParameters_rule_flagOptional() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(RuleViolationException.class, () -> parser.collectParameters(this.violatesFlagCannotBeOptional));
    }

    @Test
    public void collectParameters_unrecognizedParamType() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(IllegalArgumentException.class, () -> parser.collectParameters(this.methodWithParameterOfUnrecognizedType));
    }

    @Test
    public void collectParameters_unrecognizedParamName() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(IllegalArgumentException.class, () -> parser.collectParameters(this.methodWithParameterWithUnrecognizedName));
    }

    @Test
    public void collectParameters_ambigiousFlags() {
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(new ParameterMapperRegistry<>());
        assertThrows(IllegalStateException.class, () -> parser.collectParameters(this.methodWithAmbigiousFlags));
    }

    @Test
    public void collectParameters_01() throws RuleViolationException {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(registry);
        final Parameter[] params = this.method01.getParameters();
        final OptParam optParamAnnot = params[1].getAnnotation(OptParam.class);
        final Flag flagAnnot = params[2].getAnnotation(Flag.class);
        final List<CommandParameter<Object>> expected = List.of(
                new StandardParameter<>(
                        "arg0", false, TypeToken.of(String.class), new AnnotationList(),
                        registry.findMapper(TypeToken.of(String.class)).orElseThrow()
                ),
                new StandardParameter<>(
                        "arg1", true, TypeToken.of(Integer.TYPE), new AnnotationList(optParamAnnot),
                        registry.findMapper(TypeToken.of(Integer.class)).orElseThrow()
                ),
                new ValueFlagParameter<>(
                        "flag", 'f', "arg2", TypeToken.of(Short.TYPE), new AnnotationList(flagAnnot),
                        registry.findMapper(TypeToken.of(Short.class)).orElseThrow()
                )
        );

        final List<CommandParameter<Object>> result = parser.collectParameters(this.method01);
        assertTrue(contentEquals(expected, result));
    }

    @Test
    public void collectParameters_02() throws RuleViolationException {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(registry);
        final Parameter[] params = this.method02.getParameters();
        final Flag flagAnnot = params[1].getAnnotation(Flag.class);
        final Range rangeAnnot = params[2].getAnnotation(Range.class);
        final List<CommandParameter<Object>> expected = List.of(
                new PresenceFlagParameter<>("test", ' ', "arg1", new AnnotationList(flagAnnot)),
                new StandardParameter<>("arg2", false, TypeToken.of(Long.TYPE), new AnnotationList(rangeAnnot),
                        registry.findMapper(TypeToken.of(Long.class)).orElseThrow())
        );

        final List<CommandParameter<Object>> result = parser.collectParameters(this.method02);
        assertTrue(contentEquals(expected, result));
    }

    @Test
    public void collectParameters_03() throws RuleViolationException {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final MethodParameterParser<Object> parser = new MethodParameterParser<>(registry);
        final Parameter[] params = this.method03.getParameters();
        final Flag flagAnnot = params[1].getAnnotation(Flag.class);
        final Flag flag2Annot = params[4].getAnnotation(Flag.class);
        final Flag flag3Annot = params[7].getAnnotation(Flag.class);
        final Range rangeAnnot = params[6].getAnnotation(Range.class);
        final List<CommandParameter<Object>> expected = List.of(
                new PresenceFlagParameter<>("flag", ' ', "arg1", new AnnotationList(flagAnnot)),
                new StandardParameter<>("arg2", false, TypeToken.of(Byte.TYPE), new AnnotationList(),
                        registry.findMapper(TypeToken.of(Byte.class)).orElseThrow()),
                new StandardParameter<>("arg3", false, TypeToken.of(Short.TYPE), new AnnotationList(),
                        registry.findMapper(TypeToken.of(Short.class)).orElseThrow()),
                new ValueFlagParameter<>("flag-2", ' ', "arg4", TypeToken.of(Integer.TYPE),
                        new AnnotationList(flag2Annot), registry.findMapper(TypeToken.of(Integer.class)).orElseThrow()),
                new StandardParameter<>("arg5", false, TypeToken.of(Float.TYPE), new AnnotationList(),
                        registry.findMapper(TypeToken.of(Float.class)).orElseThrow()),
                new StandardParameter<>("arg6", false, TypeToken.of(Double.TYPE), new AnnotationList(rangeAnnot),
                        registry.findMapper(TypeToken.of(Double.class)).orElseThrow()),
                new ValueFlagParameter<>("flag-3", ' ', "arg7", TypeToken.of(Character.TYPE),
                        new AnnotationList(flag3Annot), registry.findMapper(TypeToken.of(Character.class)).orElseThrow())
        );

        final List<CommandParameter<Object>> result = parser.collectParameters(this.method03);
        assertTrue(contentEquals(expected, result));
    }

    @Test
    public void collectParameters_04() throws RuleViolationException {
        final ParameterMapperRegistry<Object> registry = new ParameterMapperRegistry<>();
        final TypeToken<List<Integer>> intsType = new TypeToken<>() {};
        final TypeToken<Set<String>> stringsType = new TypeToken<>() {};
        final TypeToken<Collection<Object>> objectsType = new TypeToken<>() {};

        registry.registerMapper(new DummyParameterMapper<>(intsType));
        registry.registerMapper(new DummyParameterMapper<>(stringsType));
        registry.registerMapper(new DummyParameterMapper<>(objectsType));

        final MethodParameterParser<Object> parser = new MethodParameterParser<>(registry);
        final List<CommandParameter<Object>> expected = List.of(
                new StandardParameter<>("arg0", false, intsType, new AnnotationList(),
                        registry.findMapper(intsType).orElseThrow()),
                new StandardParameter<>("arg1", false, stringsType, new AnnotationList(),
                        registry.findMapper(stringsType).orElseThrow()),
                new StandardParameter<>("arg2", false, objectsType, new AnnotationList(),
                        registry.findMapper(objectsType).orElseThrow())
        );

        final List<CommandParameter<Object>> actual = parser.collectParameters(this.method04);
        assertTrue(contentEquals(expected, actual));
    }

    private static boolean contentEquals(final List<CommandParameter<Object>> expected, final List<CommandParameter<Object>> result) {
        if (expected.size() != result.size()) {
            return false;
        }

        for (int i = 0; i < expected.size(); i++) {
            final CommandParameter<Object> a = expected.get(i);
            final CommandParameter<Object> b = result.get(i);
            if (!a.equals(b)) {
                return false;
            }
        }

        return true;
    }

    public static final class MethodContainer {

        public void violatesGreedyAndQuotable(final @Greedy @Quotable String string) {}

        public void violatesGreedyQuotableRegexNotString(final @Regex(".") int i) {}

        public void violatesGreedyMustBeLast(final @Greedy String string, int i) {}

        public void violatesRangeMustBeANumber(final @Range(min = 1.0D, max = 100.0D) boolean b) {}

        public void violatesSourceHasMoreAnnotations(final @Source @Quotable String source) {}

        public void violatesSourceNthParameter(final String string, final int i, final @Source Object source) {}

        public void violatesUnrecognizedAnnotation(final @Dummy String string) {}

        public void violatesFlagMustNotBeOptional(final @OptParam @Flag("flag") int i) {}

        public void methodWithParameterOfUnrecognizedType(final Object object) {}

        public void methodWithParameterWithUnrecognizedName(final @Mapper("name") Object object) {}

        public void methodWithAmbigiousFlags(final @Flag("flag") String string, final @Flag("flag") boolean b) {}

        public void method01(final String string, final @OptParam int i, final @Flag(value = "flag", shorthand = 'f') short s) {}

        public void method02(final @Source Object source, final @Flag("test") boolean b, @Range(min = 1.0D, max = 100.0D) long l) {}

        public void method03(final @Source Object source,
                             final @Flag("flag") boolean b,
                             final byte by,
                             final short s,
                             final @Flag("flag-2") int i,
                             final float f,
                             final @Range(min = 1.0D, max = 1034.54D) double d,
                             final @Flag("flag-3") char c) {}

        public void method04(final List<Integer> ints,
                             final Set<String> strings,
                             final Collection<Object> objects) {}
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Dummy {}

    private static final class DummyParameterMapper<T> implements ParameterMapper<Object, T> {
        private final TypeToken<T> type;

        DummyParameterMapper(final TypeToken<T> type) {
            this.type = type;
        }

        @Override
        public TypeToken<T> type() {
            return this.type;
        }

        @Override
        public T map(final CommandContext<Object> context,
                     final Queue<CommandInput> args,
                     final AnnotationList modifiers) throws ParameterMappingException {
            throw new UnsupportedOperationException();
        }
    }
}
