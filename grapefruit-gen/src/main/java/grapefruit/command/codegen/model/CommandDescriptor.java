package grapefruit.command.codegen.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import grapefruit.command.Command;
import grapefruit.command.annotation.CommandDefinition;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.codegen.Naming;
import grapefruit.command.codegen.util.Decorator;
import grapefruit.command.dispatcher.CommandSpec;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static com.google.auto.common.MoreElements.asExecutable;
import static com.google.auto.common.MoreElements.asType;
import static grapefruit.command.codegen.util.AnnotationUtil.accessAnnotationValue;
import static grapefruit.command.codegen.util.AnnotationUtil.accessAnnotationValueList;
import static grapefruit.command.codegen.util.AnnotationUtil.assertAnnotation;
import static java.util.Objects.requireNonNull;

public class CommandDescriptor implements Decorator {
    private final ExecutableElement method;
    private final TypeElement parent;
    private final AnnotationMirror commandDef;
    private final List<ArgumentDescriptor> arguments;
    private final String assembleArgsMethodName;

    private CommandDescriptor(
            ExecutableElement method,
            TypeElement parent,
            AnnotationMirror commandDef,
            List<ArgumentDescriptor> arguments
    ) {
        this.method = requireNonNull(method, "method cannot be null");
        this.parent = requireNonNull(parent, "parent cannot be null");
        this.commandDef = requireNonNull(commandDef, "commandDef cannot be null");
        this.arguments = requireNonNull(arguments, "arguments cannot be null");
        this.assembleArgsMethodName = method.getSimpleName() + Naming.ARGUMENTS_METHOD_SUFFIX;
    }

    public static CommandDescriptor create(Element candidate) {
        if (!candidate.getKind().equals(ElementKind.METHOD)) {
            throw new RuntimeException("Expected element to be a method");
        }

        ExecutableElement method = asExecutable(candidate);
        Element parent = candidate.getEnclosingElement();

        if (!parent.getKind().equals(ElementKind.CLASS)) {
            throw new RuntimeException("Expected parent to be a class");
        }

        if (candidate.getModifiers().contains(Modifier.STATIC)) {
            throw new RuntimeException("Command handler methods may not be static");
        }

        if (candidate.getModifiers().contains(Modifier.PRIVATE)) {
            throw new RuntimeException("Command handler methods may not be private");
        }

        return new CommandDescriptor(
                method,
                asType(parent),
                assertAnnotation(candidate, CommandDefinition.class),
                method.getParameters().stream().map(ArgumentDescriptor::create).toList()
        );
    }

    @Override
    public void decorate(TypeSpec.Builder builder) {
        builder.addMethod(generateArgumentsMethod());
        this.arguments.forEach(x -> x.decorate(builder));
    }

    public TypeElement parent() {
        return this.parent;
    }

    public CodeBlock generateInitializer() {
        String permission = accessAnnotationValue(this.commandDef, "permission", String.class);
        List<TypeMirror> conditionClasses = accessAnnotationValueList(this.commandDef, "conditions", TypeMirror.class);
        CodeBlock conditionsBlock = conditionClasses.stream()
                .map(x -> CodeBlock.of("$T.class", x))
                .collect(CodeBlock.joining(", "));

        return CodeBlock.of(
                "$T.wrap($L(), $T.of($S, $S, $L), $L)",
                Command.class,
                this.assembleArgsMethodName,
                CommandSpec.class,
                accessAnnotationValue(this.commandDef, "route", String.class),
                permission.isBlank() ? null : permission,
                conditionsBlock,
                generateCommandAction()
        );
    }

    private CodeBlock generateCommandAction() {
        return CodeBlock.of(
                "$L -> $L.$L($L)",
                Naming.CONTEXT_PARAM,
                Naming.REFERENCE_PARAM,
                this.method.getSimpleName(),
                generateArgumentList()
        );
    }

    private CodeBlock generateArgumentList() {
        return this.arguments.stream()
                .map(this::generateArgument)
                .collect(CodeBlock.joining(", "));
    }

    private CodeBlock generateArgument(ArgumentDescriptor argument) {
        if (argument.isFlag()) {
            return CodeBlock.of(
                    "$L.get($L).orElse($L)",
                    Naming.CONTEXT_PARAM,
                    argument.keyFieldName(),
                    argument.isPresenceFlag() ? false : null
            );
        }

        return CodeBlock.of("$L.require($L)", Naming.CONTEXT_PARAM, argument.keyFieldName());
    }

    private MethodSpec generateArgumentsMethod() {
        CodeBlock returnBlock = CodeBlock.builder()
                .add("$T.of(", List.class)
                .add(this.arguments.stream()
                        .filter(ArgumentDescriptor::isCommandArg)
                        .map(ArgumentDescriptor::generateArgumentInitializer)
                        .collect(CodeBlock.joining(", ")))
                .add(")")
                .build();

        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get(List.class),
                ParameterizedTypeName.get(
                        ClassName.get(CommandArgument.class),
                        WildcardTypeName.subtypeOf(TypeName.OBJECT)
                )
        );

        return MethodSpec.methodBuilder(this.assembleArgsMethodName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .returns(returnType)
                .addStatement("return $L", returnBlock)
                .build();
    }
}
