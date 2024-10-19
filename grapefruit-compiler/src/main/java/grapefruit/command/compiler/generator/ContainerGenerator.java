package grapefruit.command.compiler.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import grapefruit.command.compiler.util.ElementPredicate;
import grapefruit.command.runtime.Command;
import grapefruit.command.runtime.generated.CommandContainer;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.auto.common.MoreElements.getPackage;
import static grapefruit.command.compiler.Naming.COMMANDS_METHOD;
import static grapefruit.command.compiler.Naming.CONTAINER_CLASS_SUFFIX;
import static grapefruit.command.compiler.Naming.INTERNAL_COMMANDS_FIELD;
import static grapefruit.command.compiler.Naming.REFERENCE_PARAM;
import static grapefruit.command.compiler.util.TypeNameUtil.toTypeName;
import static java.util.Objects.requireNonNull;

/**
 * For every class containing methods annotated with
 * {@link grapefruit.command.runtime.annotation.Command}, this
 * class generates a corresponding {@link CommandContainer}
 * instance containing the {@link Command} implementations
 * generated for each method.
 */
public class ContainerGenerator implements Generator<JavaFile> {
    /*
     * Ensure the container is a non-abstract java class, that will be accessible to
     * the generated container class.
     */
    private static final ElementPredicate PREDICATE = ElementPredicate.expect(ElementKind.CLASS)
            .forbid(Modifier.PRIVATE, Modifier.ABSTRACT)
            .build();
    /* Return type of overridden method #commands */
    private static final TypeName COMMAND_SET = ParameterizedTypeName.get(Set.class, Command.class);
    /* Reference to the original container class */
    private final TypeElement container;
    /* Reference to a list of command methods found in the original container */
    private final List<CommandGenerator> commands;

    private ContainerGenerator(TypeElement container, List<CommandGenerator> commands) {
        this.container = requireNonNull(container, "container cannot be null");
        this.commands = requireNonNull(commands, "commands cannot be null");
    }

    public static ContainerGenerator create(TypeElement container, List<ExecutableElement> commandMethods) {
        // Check element
        PREDICATE.enforce(container);

        // Collect commands
        List<CommandGenerator> commands = commandMethods.stream()
                .map(CommandGenerator::create)
                .toList();

        return new ContainerGenerator(container, commands);
    }

    @Override
    public JavaFile generate(GeneratorContext context) {
        // Let the command generator include fields, methods and static imports first
        List<CodeBlock> commands = this.commands.stream()
                .map(x -> x.generate(context))
                .toList();

        AnnotationSpec generated = AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", context.generator())
                .addMember("date", "$S", Instant.now().truncatedTo(ChronoUnit.SECONDS))
                .build();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(generateOutputClassName())
                .addAnnotation(generated)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(CommandContainer.class);

        // Include fields
        context.fields().forEach(classBuilder::addField);
        // Include methods
        context.methods().forEach(classBuilder::addMethod);

        // Include our own fields
        classBuilder.addField(generateReferenceField());
        classBuilder.addField(generateCommandsField());

        // Include our own methods
        classBuilder.addMethod(generateConstructor(commands)).addMethod(generateCommandsMethod());

        // Generate java file
        JavaFile.Builder fileBuilder = JavaFile.builder(getPackage(this.container).getQualifiedName().toString(), classBuilder.build())
                .indent(" ".repeat(4))
                .skipJavaLangImports(true)
                .addStaticImport(Objects.class, "requireNonNull");

        // Add static imports
        context.staticImports().forEach(x -> fileBuilder.addStaticImport(x.from(), x.method()));
        return fileBuilder.build();
    }

    // Generate class name. This method takes nested class names into account
    private String generateOutputClassName() {
        // Collect names
        Deque<String> names = new ArrayDeque<>();
        Element element = this.container;
        // If the element is a package, stop
        while (element != null && !element.getKind().equals(ElementKind.PACKAGE)) {
            names.offerFirst(element.getSimpleName().toString());
            element = element.getEnclosingElement();
        }

        String baseName = String.join("_", names);
        return CONTAINER_CLASS_SUFFIX.apply(baseName);
    }

    private FieldSpec generateReferenceField() {
        return FieldSpec.builder(toTypeName(this.container), REFERENCE_PARAM, Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private FieldSpec generateCommandsField() {
        return FieldSpec.builder(COMMAND_SET, INTERNAL_COMMANDS_FIELD, Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private MethodSpec generateConstructor(List<CodeBlock> commands) {
        // Generate "internalCommands" initializer
        CodeBlock initializer = CodeBlock.builder()
                .add("$T.of(", Set.class)
                .indent()
                .add(commands.stream().collect(CodeBlock.joining(",\n")))
                .unindent()
                .add(")")
                .build();

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(toTypeName(this.container), REFERENCE_PARAM)
                .addStatement("this.$L = requireNonNull($L, $S)", REFERENCE_PARAM, REFERENCE_PARAM, "reference cannot be null")
                .addStatement("this.$L = $L", INTERNAL_COMMANDS_FIELD, initializer)
                .build();
    }

    private MethodSpec generateCommandsMethod() {
        return MethodSpec.methodBuilder(COMMANDS_METHOD)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(COMMAND_SET)
                // Create immutable copy of the command set
                .addStatement("return $T.copyOf(this.$L)", Set.class, INTERNAL_COMMANDS_FIELD)
                .build();
    }
}
