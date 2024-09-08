package grapefruit.command.codegen.generator;

import com.squareup.javapoet.JavaFile;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * For every class containing methods annotated with
 * {@link grapefruit.command.annotation.CommandDefinition}, this
 * class generates a corresponding {@link grapefruit.command.CommandContainer}
 * instance containing the {@link grapefruit.command.Command} implementations
 * generated for each method.
 */
public class ContainerGenerator implements Generator<JavaFile> {
    /* Reference to the original container class */
    private final TypeElement container;
    /* Reference to a list of command methods found in the original container */
    private final List<ExecutableElement> commandMethods;

    public ContainerGenerator(TypeElement container, List<ExecutableElement> commandMethods) {
        this.container = requireNonNull(container, "container cannot be null");
        this.commandMethods = requireNonNull(commandMethods, "commandMethods cannot be null");
    }

    @Override
    public JavaFile generate(GeneratorContext context) {
        throw new UnsupportedOperationException();
    }
}
