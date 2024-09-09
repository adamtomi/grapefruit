package grapefruit.command.codegen.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import grapefruit.command.Command;
import grapefruit.command.annotation.CommandDefinition;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.codegen.util.ElementPredicate;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandSpec;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static grapefruit.command.codegen.Naming.ACTION_METHOD_SUFFIX;
import static grapefruit.command.codegen.Naming.ARGUMENTS_METHOD_SUFFIX;
import static grapefruit.command.codegen.Naming.CONTEXT_PARAM;
import static grapefruit.command.codegen.Naming.REFERENCE_PARAM;
import static grapefruit.command.codegen.Naming.RESULT;
import static grapefruit.command.codegen.util.AnnotationUtil.accessAnnotationValue;
import static grapefruit.command.codegen.util.AnnotationUtil.accessAnnotationValueList;
import static grapefruit.command.codegen.util.AnnotationUtil.assertAnnotation;
import static grapefruit.command.codegen.util.StringUtil.pick;
import static java.util.Objects.requireNonNull;

/**
 * This class is responsible for generating two methods for every method
 * annotated with {@link grapefruit.command.annotation.CommandDefinition}:
 * <ul>
 *     <li>One that returns the argument list of that command</li>
 *     <li>One that handles command invocation (and invokes the original command method)</li>
 * </ul>
 */
public class CommandGenerator implements Generator<CodeBlock> {
    /*
     * Ensure the command method is a non-private, non-static, non-abstract method, that
     * will be available to our generated method.
     */
    private static final ElementPredicate PREDICATE = ElementPredicate.expect(ElementKind.METHOD)
            .forbid(Modifier.PRIVATE, Modifier.STATIC, Modifier.ABSTRACT)
            .build();
    /* Return type of <commandMethod>$arguments */
    private static final TypeName COMMAND_ARG_LIST = ParameterizedTypeName.get(
            ClassName.get(List.class),
            ParameterizedTypeName.get(
                    ClassName.get(CommandArgument.class),
                    WildcardTypeName.subtypeOf(TypeName.OBJECT)
            )
    );
    private final ExecutableElement method;
    private final AnnotationMirror commandDef;
    private final List<ParameterGenerator> parameters;

    private CommandGenerator(ExecutableElement method, AnnotationMirror commandDef, List<ParameterGenerator> parameters) {
        this.method = requireNonNull(method, "method cannot be null");
        this.commandDef = requireNonNull(commandDef, "commandDef cannot be null");
        this.parameters = requireNonNull(parameters, "parameters cannot be null");
    }

    public static CommandGenerator create(ExecutableElement method) {
        // Check element
        PREDICATE.ensure(method);

        // Collect parameters
        List<ParameterGenerator> parameters = method.getParameters()
                .stream()
                .map(ParameterGenerator::create)
                .toList();

        return new CommandGenerator(
                method,
                assertAnnotation(method, CommandDefinition.class),
                parameters
        );
    }

    @Override
    public CodeBlock generate(GeneratorContext context) {
        // Extract codeblocks from parameters
        List<ParameterGenerator.Result> parameters = this.parameters.stream()
                .map(x -> x.generate(context))
                .toList();

        // Include arguments method
        context.include(generateArgumentsMethod(parameters));
        // Include our command handler method
        context.include(generateActionMethod(parameters));
        // Static import Command#wrap
        context.importStatic(Command.class, "wrap");

        return CodeBlock.of(
                "wrap($L, $L, this::$L)",
                ARGUMENTS_METHOD_SUFFIX.apply(this.method),
                generateCommandSpec(),
                ACTION_METHOD_SUFFIX.apply(this.method)
        );
    }

    private MethodSpec generateArgumentsMethod(List<ParameterGenerator.Result> parameters) {
        // Build the actual code block holding the list of command arguments
        CodeBlock initializer = CodeBlock.builder()
                .add("$T $L = $T.of(\n", COMMAND_ARG_LIST, RESULT, List.class)
                .indent()
                .add(parameters.stream()
                        .map(ParameterGenerator.Result::initializer)
                        .collect(CodeBlock.joining(",\n")))
                .unindent()
                .add("\n)")
                .build();

        // Build the method. This will be called by Command#wrap later.
        return MethodSpec.methodBuilder(ARGUMENTS_METHOD_SUFFIX.apply(this.method))
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .returns(COMMAND_ARG_LIST)
                .addCode(initializer)
                .addStatement("return $L", RESULT)
                .build();
    }

    // This method will be responsible for calling the original command method.
    private MethodSpec generateActionMethod(List<ParameterGenerator.Result> parameters) {
        // Generate code block calling the original command method
        CodeBlock call = CodeBlock.builder()
                .add("this.$L.$L(", REFERENCE_PARAM, this.method.getSimpleName())
                .indent()
                .add(parameters.stream()
                        .map(ParameterGenerator.Result::valueExtractor)
                        .collect(CodeBlock.joining(",\n")))
                .unindent()
                .add("\n)")
                .build();

        // Build the method. This will be passed to Command#wrap later.
        return MethodSpec.methodBuilder(ACTION_METHOD_SUFFIX.apply(this.method))
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addParameter(CommandContext.class, CONTEXT_PARAM)
                .addCode(call)
                .build();
    }

    // Generates the CommandSpec belonging to this command
    private CodeBlock generateCommandSpec() {
        // Retrieve route
        String route = accessAnnotationValue(this.commandDef, "route", String.class);
        // Retrieve permission. Default to null, if the value was a blank string
        String permission = pick(accessAnnotationValue(this.commandDef, "permission", String.class), null);
        // Convert condition classes
        CodeBlock conditions = accessAnnotationValueList(this.commandDef, "conditions", TypeMirror.class).stream()
                .map(x -> CodeBlock.of("$T.class", x))
                .collect(CodeBlock.joining(", "));

        return CodeBlock.of(
                "$T.of($S, $S, $L)",
                CommandSpec.class,
                route,
                permission,
                conditions
        );
    }
}
