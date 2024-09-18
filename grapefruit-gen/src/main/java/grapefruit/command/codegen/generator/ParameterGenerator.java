package grapefruit.command.codegen.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import grapefruit.command.annotation.argument.Arg;
import grapefruit.command.annotation.argument.Flag;
import grapefruit.command.annotation.inject.InjectedBy;
import grapefruit.command.annotation.mapper.MappedBy;
import grapefruit.command.argument.CommandArguments;
import grapefruit.command.argument.modifier.ContextualModifier;
import grapefruit.command.argument.modifier.ModifierBlueprint;
import grapefruit.command.argument.modifier.ModifierChain;
import grapefruit.command.codegen.Naming;
import grapefruit.command.util.key.Key;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValuesWithDefaults;
import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreElements.getAnnotationMirror;
import static grapefruit.command.codegen.Naming.CONTEXT_PARAM;
import static grapefruit.command.codegen.util.AnnotationUtil.accessAnnotationValue;
import static grapefruit.command.codegen.util.AnnotationUtil.findAnnotation;
import static grapefruit.command.codegen.util.AnnotationUtil.matches;
import static grapefruit.command.codegen.util.CodeBlockUtil.key;
import static grapefruit.command.codegen.util.StringUtil.pick;
import static grapefruit.command.codegen.util.StringUtil.sanitize;
import static grapefruit.command.codegen.util.StringUtil.toKebabCase;
import static grapefruit.command.codegen.util.TypeNameUtil.flattenTypeNames;
import static grapefruit.command.codegen.util.TypeNameUtil.toTypeName;
import static java.util.Objects.requireNonNull;

/**
 * Generates code for a single method parameter. These parameters may fall into
 * three categories:
 * <ul>
 *     <li>
 *         Non-command-argument parameters: Required, the command context will be expected
 *         to hold a matching key, however, they are not part of the argument list,
 *         thus their value isn't calculated from user input.
 *     </li>
 *     <li>
 *         Required argument parameters: Required command arguments, the user needs
 *         to provide a valid value for them.
 *     </li>
 *     <li>
 *         Flag argument parameters: Flag (or optional) command arguments, the user can
 *         provide a value, if it's desired.
 *     </li>
 * </ul>
 */
public abstract class ParameterGenerator implements Generator<ParameterGenerator.Result> {
    protected final TypeName typeName;
    protected final String keyFieldName;

    private ParameterGenerator(TypeName typeName, String keyFieldName) {
        this.typeName = requireNonNull(typeName, "typeName cannot be null");
        this.keyFieldName = sanitize(requireNonNull(keyFieldName, "keyFieldName cannot be null"));
    }

    public static ParameterGenerator create(VariableElement element) {
        TypeName typeName = toTypeName(element).box();
        // Retrieve @Arg annotation, if it exists
        Optional<AnnotationMirror> arg = getAnnotationMirror(element, Arg.class).toJavaUtil();
        // Retrieve @Flag annotation, if it exists
        Optional<AnnotationMirror> flag = getAnnotationMirror(element, Flag.class).toJavaUtil();

        /*
         * If neither are present, we're dealing with a parameter, that's not going to be
         * turned into a command argument. Upon execution, the command context will be
         * expected to hold a value with a matching key for such parameters.
         */
        if (arg.isEmpty() && flag.isEmpty()) {
            // AnnotationMirror injectedBy = assertAnnotation(element, InjectedBy.class);
            AnnotationMirror injectedBy = findAnnotation(element, InjectedBy.class)
                    .orElseThrow(() -> new IllegalStateException("Non-argument parameters are expected to be annotated by '%s'".formatted(InjectedBy.class)));
            return new NonArgument(typeName, injectedBy);
        }

        // Fallback name, if @Arg or @Flag doesn't provide a custom one.
        String fallbackName = element.getSimpleName().toString();

        // Extract mapper name, if possible
        String mapperName = findAnnotation(element, MappedBy.class)
                .map(x -> accessAnnotationValue(x, "value", String.class))
                .map(x -> pick(x, null)) // Blank strings aren't valid, default to null instead
                .orElse(null);

        Map<AnnotationMirror, TypeMirror> modifiers = extractModifiers(element);

        if (arg.isPresent()) {
            // A parameter can be annotated with either, but not both
            if (flag.isPresent()) throw new IllegalStateException("Element '%s' is annotated with both @Arg and @Flag.".formatted(element));

            // Use the correct name
            String argumentName = pick(accessAnnotationValue(arg.orElseThrow(), "name", String.class), fallbackName);
            return new RequiredArg(typeName, argumentName, mapperName, modifiers);
        } else { // This means that "flag" is present
            AnnotationMirror flagDef = flag.orElseThrow();
            // Use the correct name
            String argumentName = pick(accessAnnotationValue(flagDef, "name", String.class), fallbackName);
            char shorthand = accessAnnotationValue(flagDef, "shorthand", Character.class);

            // Turn to kebab-case, if necessary
            String finalName = accessAnnotationValue(flagDef, "hyphenate", Boolean.class)
                    ? toKebabCase(argumentName)
                    : argumentName;
            return new FlagArg(typeName, finalName, mapperName, shorthand, modifiers);
        }
    }

    // Extract annotations annotated by @Modifier or @Modifier.Factory.
    // The key is going to be the annotation found on the method parameter,
    // whereas the value is the @Modifier or @Modifier.Factory annotation
    // found on the annotation used as a key.
    private static Map<AnnotationMirror, TypeMirror> extractModifiers(VariableElement parameter) {
         Map<AnnotationMirror, TypeMirror> result = new HashMap<>();
         for (AnnotationMirror annotation : parameter.getAnnotationMirrors()) {
             for (AnnotationMirror _annotation : asType(annotation.getAnnotationType().asElement()).getAnnotationMirrors()) {
                 if (matches(_annotation, grapefruit.command.annotation.modifier.Modifier.class)
                         || matches(_annotation, grapefruit.command.annotation.modifier.Modifier.Factory.class)) {
                     result.put(annotation, accessAnnotationValue(_annotation, "value", TypeMirror.class));
                     // We found a matching annotation, so move on to the next one (on the parameter)
                     break;
                 }
             }
         }

         return result;
    }

    private static String keyFieldName(TypeName typeName) {
        return flattenTypeNames(typeName).stream()
                .map(TypeName::toString)
                .map(x -> x.replaceAll("\\.", "_"))
                .collect(Collectors.joining("_")) + Naming.KEY_FIELD_SUFFIX;
    }

    @Override
    public Result generate(GeneratorContext context) {
        // Include field generated for this argument's key
        context.include(generateKeyField());

        return new Result(generateInitializer(), generateValueExtractor());
    }

    /* The resulting CodeBlock will be inserted into the generated argument list. */
    protected abstract CodeBlock generateInitializer();

    /* The resulting CodeBlock determines how to extract a value from a command context. */
    protected abstract CodeBlock generateValueExtractor();

    protected CodeBlock generateKeyInitializer() {
        return key(this.typeName, null);
    }

    /* Generates key field that's used to retrieve values from the command context. */
    private FieldSpec generateKeyField() {
        return FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Key.class), this.typeName), this.keyFieldName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(generateKeyInitializer())
                .build();
    }

    public record Result(CodeBlock initializer, CodeBlock valueExtractor) {}

    /* Represents a non-command-argument parameter */
    private static final class NonArgument extends ParameterGenerator {
        private final AnnotationMirror injectedBy;

        private NonArgument(TypeName typeName, AnnotationMirror injectedBy) {
            super(typeName, "%s_%s".formatted(
                    accessAnnotationValue(injectedBy, "value", String.class),
                    ParameterGenerator.keyFieldName(typeName)
            ));
            this.injectedBy = requireNonNull(injectedBy, "injectedBy cannot be null");
        }

        @Override
        protected CodeBlock generateInitializer() {
            // Does not have an initializer
            return null;
        }

        @Override
        protected CodeBlock generateKeyInitializer() {
            return key(this.typeName, accessAnnotationValue(this.injectedBy, "value", String.class));
        }

        @Override
        protected CodeBlock generateValueExtractor() {
            boolean nullable = accessAnnotationValue(this.injectedBy, "nullable", Boolean.class);
            return CodeBlock.of(
                    "$L.$L($L)",
                    CONTEXT_PARAM,
                    nullable ? "nullable" : "require",
                    this.keyFieldName);
        }
    }

    /* Base class for command argument parameters */
    private static abstract class Argument extends ParameterGenerator {
        protected final String name;
        protected final String mapperName;
        protected final Map<AnnotationMirror, TypeMirror> modifiers;

        private Argument(TypeName typeName, String name, /* @Nullable */ String mapperName, Map<AnnotationMirror, TypeMirror> modifiers) {
            super(typeName, "%s_%s".formatted(name, ParameterGenerator.keyFieldName(typeName)));
            this.name = requireNonNull(name, "name cannot be null");
            this.mapperName = mapperName; // mapperName CAN be null
            this.modifiers = requireNonNull(modifiers, "modifiers cannot be null");
        }

        protected CodeBlock generateMapperKey() {
            return key(this.typeName, this.mapperName);
        }

        @Override
        protected CodeBlock generateKeyInitializer() {
            return key(this.typeName, this.name);
        }

        protected CodeBlock generateModifierChain() {
            CodeBlock modifiers = this.modifiers.entrySet()
                    .stream()
                    .map(this::generateSingleModifier)
                    .collect(CodeBlock.joining(", "));

            return CodeBlock.of("$T.of($T.of($L))", ModifierChain.class, List.class, modifiers);
        }

        // Generates a single ModifierBlueprint
        private CodeBlock generateSingleModifier(Map.Entry<AnnotationMirror, TypeMirror> modifier) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = getAnnotationValuesWithDefaults(modifier.getKey()); // modifier.getKey().getElementValues();

            // Generate Context builder
            CodeBlock modifierContext;
            if (values.isEmpty()) {
                modifierContext = CodeBlock.of("null");
            } else {
                modifierContext = CodeBlock.builder().add("$T.builder().", ContextualModifier.Context.class)
                        .add(values.entrySet().stream()
                                .map(x -> CodeBlock.of("put($S, $L)", x.getKey().getSimpleName(), x.getValue()))
                                .collect(CodeBlock.joining(".")))
                        .add(".build()")
                        .build();
            }

            // Generate ModifierBlueprint.of()
            return CodeBlock.of("$T.of($T.class, $L)", ModifierBlueprint.class, modifier.getValue(), modifierContext);
        }
    }

    /* Represents a required command argument parameter */
    private static final class RequiredArg extends Argument {
        private RequiredArg(TypeName typeName, String name, String mapperName, Map<AnnotationMirror, TypeMirror> modifiers) {
            super(typeName, name, mapperName, modifiers);
        }

        @Override
        protected CodeBlock generateInitializer() {
            return CodeBlock.of("required($S, $L, $L, $L)", this.name, this.keyFieldName, generateMapperKey(), generateModifierChain());
        }

        @Override
        protected CodeBlock generateValueExtractor() {
            return CodeBlock.of("$L.require($L)", CONTEXT_PARAM, this.keyFieldName);
        }

        @Override
        public Result generate(GeneratorContext context) {
            context.importStatic(CommandArguments.class, "required");
            return super.generate(context);
        }
    }

    /* Represents a flag command argument parameter */
    private static final class FlagArg extends Argument {
        private final boolean presence;
        private final char shorthand;

        private FlagArg(TypeName typeName, String name, String mapperName, char shorthand, Map<AnnotationMirror, TypeMirror> modifiers) {
            super(typeName, name, mapperName, modifiers);
            this.presence = typeName.equals(TypeName.BOOLEAN);
            this.shorthand = !Character.isAlphabetic(shorthand) ? name.charAt(0) : shorthand;
        }

        @Override
        protected CodeBlock generateInitializer() {
            return this.presence
                    ? CodeBlock.of("presenceFlag($S, '$L', $L)", this.name, this.shorthand, generateMapperKey())
                    : CodeBlock.of("valueFlag($S, '$L', $L, $L, $L)", this.name, this.shorthand, this.keyFieldName, generateMapperKey(), generateModifierChain());
        }

        @Override
        protected CodeBlock generateValueExtractor() {
            return this.presence
                    ? CodeBlock.of("$L.get($L).orElse($L)", CONTEXT_PARAM, this.keyFieldName, false)
                    : CodeBlock.of("$L.nullable($L)", CONTEXT_PARAM, this.keyFieldName);
        }

        @Override
        public Result generate(GeneratorContext context) {
            context.importStatic(CommandArguments.class, this.presence ? "presenceFlag" : "valueFlag");
            return super.generate(context);
        }
    }
}
