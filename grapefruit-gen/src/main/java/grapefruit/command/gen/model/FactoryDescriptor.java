package grapefruit.command.gen.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import grapefruit.command.Command;
import grapefruit.command.CommandFactory;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.gen.Naming;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.google.auto.common.MoreElements.getPackage;
import static grapefruit.command.gen.util.TypeNameUtil.toTypeName;
import static java.util.Objects.requireNonNull;

public class FactoryDescriptor {
    private final TypeElement classFile;
    private final List<CommandDescriptor> commands;

    public FactoryDescriptor(TypeElement classFile, List<CommandDescriptor> commands) {
        this.classFile = requireNonNull(classFile, "classFile cannot be null");
        this.commands = requireNonNull(commands, "commands cannot be null");
    }

    public JavaFile generateFile() {
        TypeSpec outClass = generateClassFile();
        return JavaFile.builder(getPackage(this.classFile).getQualifiedName().toString(), outClass)
                .addFileComment("") // TODO file header
                .indent(" ".repeat(4))
                .build();
    }

    private TypeSpec generateClassFile() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(this.classFile.getSimpleName() + Naming.FACTORY_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(CommandFactory.class), toTypeName(this.classFile)));
        this.commands.forEach(x -> x.decorate(classBuilder));

        return classBuilder.addField(generateInternalFactoyField())
                .addMethod(generateCreateCommandsMethods())
                .build();
    }

    private FieldSpec generateInternalFactoyField() {
        CodeBlock fieldInitializer = CodeBlock.builder()
                .add("$L -> $T.of(\n", Naming.REFERENCE_PARAM, Set.class)
                .indent()
                .add(this.commands.stream()
                        .map(CommandDescriptor::generateInitializer)
                        .collect(CodeBlock.joining(",\n")))
                .unindent()
                .add(")")
                .build();

        return FieldSpec.builder(
                ParameterizedTypeName.get(
                        ClassName.get(Function.class),
                        toTypeName(this.classFile),
                        ParameterizedTypeName.get(Set.class, Command.class)
                ),
                Naming.INTERNAL_FACTORY_FIELD,
                Modifier.PRIVATE,
                Modifier.FINAL
        )
                .initializer(fieldInitializer)
                .build();
    }

    private MethodSpec generateCreateCommandsMethods() {
        return MethodSpec.methodBuilder(Naming.GENERATE_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(Set.class, Command.class))
                .addParameter(CommandContext.class, Naming.CONTEXT_PARAM)
                .addStatement("return this.$L.apply($L)", Naming.INTERNAL_FACTORY_FIELD, Naming.CONTEXT_PARAM)
                .build();
    }
}
