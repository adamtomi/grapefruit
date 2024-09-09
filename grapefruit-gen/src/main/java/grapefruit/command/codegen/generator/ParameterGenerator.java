package grapefruit.command.codegen.generator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

import static java.util.Objects.requireNonNull;

/*
 * Non-command-argument: @InjectedBy (or annotations annotated with it)
 * Command Argument
 *   - Required argument (@Arg)
 *   - Presence flag (@Flag)
 *   - Value flag (@Flag)
 */
public abstract class ParameterGenerator implements Generator<ParameterGenerator.Result> {
    private final TypeName type;

    private ParameterGenerator(TypeName type) {
        this.type = requireNonNull(type, "type cannot be null");
    }

    public static ParameterGenerator create(VariableElement element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Result generate(GeneratorContext context) {
        throw new UnsupportedOperationException("");
    }

    public record Result(CodeBlock initializer, CodeBlock valueExtractor) {}
}
