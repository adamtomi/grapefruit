package grapefruit.command.codegen.generator;

/**
 * Responsible for generating some piece of code.
 *
 * @see CommandGenerator
 * @see ContainerGenerator
 * @see ParameterGenerator
 * @param <T> The result of the generation process
 */
public interface Generator<T> {

    /**
     * Perform the generation process using the provided context.
     *
     * @param context The context
     * @return The result
     */
    T generate(GeneratorContext context);
}
