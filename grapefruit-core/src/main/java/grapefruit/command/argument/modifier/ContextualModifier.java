package grapefruit.command.argument.modifier;

import grapefruit.command.util.key.Key;

/**
 * Contextual modifiers are used, when the corresponding {@link grapefruit.command.annotation.modifier.Modifier}
 * annotation has values, that should be made accessible to the modifier. In such cases,
 * a {@link Factory} factory can be registered instead of an argument modifier instance.
 * This factory will receive a {@link Context} built from the values extracted from said
 * annotation.
 *
 * @param <T> The expected data type
 */
public interface ContextualModifier<T> extends ArgumentModifier<T> {

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
     * a {@link Context}. Every {@link ContextualModifier} implementation
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
        ContextualModifier<T> createFromContext(Context context);
    }
}
