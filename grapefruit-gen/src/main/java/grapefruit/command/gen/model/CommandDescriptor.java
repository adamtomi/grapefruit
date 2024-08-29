package grapefruit.command.gen.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import grapefruit.command.Command;
import grapefruit.command.CommandDefinition;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.dispatcher.CommandMeta;
import grapefruit.command.gen.Naming;
import grapefruit.command.gen.util.CodeBlockUtil;
import grapefruit.command.gen.util.Decorator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;

import static com.google.auto.common.MoreElements.asExecutable;
import static com.google.auto.common.MoreElements.asType;
import static grapefruit.command.gen.util.AnnotationUtil.accessAnnotationValue;
import static grapefruit.command.gen.util.AnnotationUtil.assertAnnotation;
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
        this.assembleArgsMethodName = method.getSimpleName() + Naming.ASSEMBLE_ARGUMENTS_METHOD_SUFFIX;
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
        builder.addMethod(generateAssembleArgsMethod());
        this.arguments.forEach(x -> x.decorate(builder));
    }

    public TypeElement parent() {
        return this.parent;
    }

    public CodeBlock generateInitializer() {
        return CodeBlock.of(
                "$T.wrap($T.of($S, $L()), $L)",
                Command.class,
                CommandMeta.class,
                accessAnnotationValue(this.commandDef, "route", String.class),
                this.assembleArgsMethodName,
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
        return CodeBlockUtil.join(
                ", ",
                this.arguments.stream()
                        .map(x -> CodeBlock.of("$L.get($L)", Naming.CONTEXT_PARAM, x.keyFieldName()))
                        .toList()
        );
    }

    private MethodSpec generateAssembleArgsMethod() {
        CodeBlock returnBlock = CodeBlock.builder()
                .addStatement("return $T.of(\n", List.class)
                .indent()
                .add(CodeBlockUtil.join(",\n", this.arguments.stream()
                        .filter(ArgumentDescriptor::isCommandArg)
                        .map(ArgumentDescriptor::generateArgumentInitializer)
                        .toList()))
                .unindent()
                .add("\n)")
                .build();

        return MethodSpec.methodBuilder(this.assembleArgsMethodName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), WildcardTypeName.get(CommandArgument.class)))
                .addCode(returnBlock)
                .build();
    }
}
