package grapefruit.command.argument.modifier;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.key.Key;

/**
 * Argument modifiers can be used to:
 * <ul>
 *     <li>Verify, that the input meets certain criteria</li>
 *     <li>Can alter the input (but cannot change the its type)</li>
 * </ul>
 * They receive input mapped by an {@link grapefruit.command.argument.mapper.ArgumentMapper}
 * which they can then operate on.
 *
 * @param <T> The expected data type
 */
public interface ArgumentModifier<T> {

    /**
     * Operates on the supplied input and returns the result,
     * or throws an exception, if the data is deemed invalid.
     *
     * @param input The input
     * @return The modified argument
     * @throws CommandArgumentException If the argument does not
     * meet certain criteria
     */
    T apply(T input) throws CommandArgumentException;

    /**
     * Returns a new {@link ArgumentModifierException} instance assoicated with
     * this modifier instance.
     *
     * @param input The user input that was parsed
     * @return The generated exception
     */
    default ArgumentModifierException generateException(String input) {
        return new ArgumentModifierException(input, this);
    }

    /**
     * The context is responsible for storing values extracted from the original
     * annotation. A named {@link Key} will be created for every extracted value
     * with its type being the type of the annotation value. Which means that if
     * we have the following annotation
     * <pre>{@code
     * @Retention(RetentionPolicy.SOURCE)
     * @Target(ElementType.PARAMETER)
     * @Modifier.Factory(MyModifierFactory.class)
     * public @interface MyModifier {
     *
     *     String customValue();
     * }
     * }</pre>
     *
     * the extracted value will be stored under {@code Key.named(String.class, "customValue")}.
     */
    interface Context {

        /**
         * Retrieve an extracted value from this context. The value will always be
         * non-null (as null values are not allowed in annotations).
         *
         * @param <T> The expected data type
         * @param key The key
         * @return The value mapped to the supplied key
         */
        <T> T require(Key<T> key);

        /**
         * Creates a new context builder. Used be the code generator.
         *
         * @return The builder instance
         */
        static Builder builder() {
            return new ModifierImpl.ContextBuilderImpl();
        }

        /**
         * Context builder, used by the code generator.
         */
        interface Builder {

            /**
             * Stores the provided value in the internal map
             * by the provided key.
             *
             * @param key The key
             * @param value THe value
             * @return This builder instance
             */
            Builder put(String key, Object value);

            /**
             * Builds a {@link Context} instance from the stored data.
             *
             * @return The created context
             */
            Context build();
        }
    }

    /**
     * Factories are used to create contextual modifier instances from
     * a {@link ArgumentModifier.Context}. Every {@link ArgumentModifier} implementation
     * is expected to have a corresponding factory implementation.
     *
     * @param <T> The data type expected by the modifier
     */
    interface Factory<T> {

        /**
         * Creates and returns the created modifier instance.
         *
         * @param context The context
         * @return The created modifier instance
         */
        ArgumentModifier<T> createFromContext(Context context);

        /**
         * Creates a factory instance that always returns the supplied
         * {@link ArgumentModifier} instance.
         *
         * @param <T> The data type expected by the modifier
         * @param modifier The modifier instance
         * @return The created factory instance
         */
        static <T> Factory<T> providing(ArgumentModifier<T> modifier) {
            return ctx -> modifier;
        }
    }
}
