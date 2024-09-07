package grapefruit.command.gen.model;

import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import grapefruit.command.annotation.arg.Arg;
import grapefruit.command.annotation.arg.Flag;
import grapefruit.command.argument.CommandArguments;
import grapefruit.command.gen.Naming;
import grapefruit.command.gen.util.Decorator;
import grapefruit.command.gen.util.TypeNameUtil;
import grapefruit.command.util.key.Key;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.auto.common.MoreElements.getAnnotationMirror;
import static grapefruit.command.gen.util.AnnotationUtil.accessAnnotationValue;
import static grapefruit.command.gen.util.TypeNameUtil.toTypeName;
import static java.util.Objects.requireNonNull;

public class ArgumentDescriptor implements Decorator {
    private final VariableElement parameter;
    private final String name;
    // Only set, if this argument represents a flag (that has a shorthand)
    private final char shorthand;
    /*
     * Set to true, if the represented argument is a command argument
     * (meaning it is part of the expected command input).
     */
    private final boolean isCommandArg;
    /* Set to true, if the represented argument is a flag */
    private final boolean isFlag;
    private final String keyFieldName;
    private final TypeName boxedType;

    private ArgumentDescriptor(
            VariableElement parameter,
            String name,
            char shorthand,
            boolean isCommandArg,
            boolean isFlag
    ) {
        this.parameter = requireNonNull(parameter, "parameter cannot be null");
        this.name = requireNonNull(name, "name cannot be null");
        this.shorthand = shorthand;
        this.isCommandArg = isCommandArg;
        this.isFlag = isFlag;
        String partialKeyFieldName = TypeNameUtil.collectParameterizedTypeNames(toTypeName(parameter)).stream()
                .map(TypeName::toString)
                .map(x -> x.replaceAll("\\.", "_"))
                .collect(Collectors.joining("_")) + Naming.KEY_FIELD_SUFFIX;

        this.keyFieldName = isCommandArg
                ? "%s_%s".formatted(name, partialKeyFieldName)
                : partialKeyFieldName;
        this.boxedType = toTypeName(parameter).box();
    }

    public static ArgumentDescriptor create(VariableElement parameter) {
        String name = parameter.getSimpleName().toString();
        char shorthand = ' ';
        boolean isCommandArg = false;
        boolean isFlag = false;

        Optional<AnnotationMirror> argMirror = getAnnotationMirror(parameter, Arg.class)
                .toJavaUtil();
        Optional<AnnotationMirror> flagMirror = getAnnotationMirror(parameter, Flag.class)
                .toJavaUtil();

        if (argMirror.isPresent()) {
            // Don't allow both annotations to be present
            if (flagMirror.isPresent()) {
                throw new RuntimeException("Element '%s' annotated with both '%s' and '%s'".formatted(
                        parameter,
                        Arg.class.getCanonicalName(),
                        Flag.class.getCanonicalName()
                ));
            }

            isCommandArg = true;
        } else if (flagMirror.isPresent()) {
            isCommandArg = true;
            isFlag = true;
            name = accessAnnotationValue(flagMirror.orElseThrow(), "name", String.class);
            shorthand = accessAnnotationValue(flagMirror.orElseThrow(), "shorthand", Character.class);
        }

        return new ArgumentDescriptor(
                parameter,
                name,
                shorthand,
                isCommandArg,
                isFlag
        );
    }

    @Override
    public void decorate(TypeSpec.Builder builder) {
        FieldSpec field = generateKeyField();
        if (!builder.fieldSpecs.contains(field)) builder.addField(field);
    }

    public String keyFieldName() {
        return this.keyFieldName;
    }

    public boolean isCommandArg() {
        return this.isCommandArg;
    }

    public boolean isFlag() {
        return this.isFlag;
    }

    public boolean isPresenceFlag() {
        return this.isFlag && toTypeName(this.parameter).equals(TypeName.BOOLEAN);
    }

    public CodeBlock generateArgumentInitializer() {
        if (!this.isCommandArg) throw new UnsupportedOperationException("Non-command-arguments cannot have initializers");

        if (this.isFlag) {
            return isPresenceFlag()
                    ? CodeBlock.of("$T.presenceFlag($S, '$L', $L)", CommandArguments.class, this.name, this.shorthand, generateMapperKeyInitializer())
                    : CodeBlock.of("$T.valueFlag($S, '$L', $L, $L)", CommandArguments.class, this.name, this.shorthand, generateKeyInitializer(),
                        generateMapperKeyInitializer());
        }

        return CodeBlock.of("$T.standard($S, $L, $L)", CommandArguments.class, this.name, generateKeyInitializer(), generateMapperKeyInitializer());
    }

    private CodeBlock generateTypeToken() {
        String typeTokenInit = this.boxedType instanceof ParameterizedTypeName
                ? "new $T<$T>() {}"
                : "$T.of($T.class)";

        return CodeBlock.of(typeTokenInit, TypeToken.class, this.boxedType);
    }

    private CodeBlock generateKeyInitializer() {
        CodeBlock typeToken = generateTypeToken();
        return this.isCommandArg
                ? CodeBlock.of("$T.named($L, $S)", Key.class, typeToken, this.name)
                : CodeBlock.of("$T.of($L)", Key.class, typeToken);
    }

    // TODO named mapper support
    private CodeBlock generateMapperKeyInitializer() {
        CodeBlock typeToken = generateTypeToken();
        return CodeBlock.of("$T.of($L)", Key.class, typeToken);
    }

    private FieldSpec generateKeyField() {
        return FieldSpec.builder(
                ParameterizedTypeName.get(ClassName.get(Key.class), this.boxedType),
                this.keyFieldName,
                Modifier.PRIVATE,
                Modifier.STATIC,
                Modifier.FINAL
        )
                .initializer(generateKeyInitializer())
                .build();
    }
}
