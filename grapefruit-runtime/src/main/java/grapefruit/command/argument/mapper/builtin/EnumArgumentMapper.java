package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.EnumSet;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * An enum argument mapper maps strings into enum values, if possible. A list of
 * valid values need to be passed to it upon creation.
 * @see EnumSet
 */
public final class EnumArgumentMapper<E extends Enum<E>> implements ArgumentMapper<E> {
    private final EnumSet<E> allowedValues;
    private final ResolutionStrategy<E> resolutionStrategy;
    private final CompletionTransformer<E> completionTransformer;

    private EnumArgumentMapper(EnumSet<E> allowedValues, ResolutionStrategy<E> resolutionStrategy, CompletionTransformer<E> completionTransformer) {
        this.allowedValues = requireNonNull(allowedValues, "allowedValues cannot be null");
        this.resolutionStrategy = requireNonNull(resolutionStrategy, "resolutionStrategy cannot be null");
        this.completionTransformer = requireNonNull(completionTransformer, "completionTransformer cannot be null");
    }

    /**
     * Constructs a new {@link EnumArgumentMapper} instance from the supplied
     * values. By default {@link ResolutionStrategy#strict()} is used to resolve
     * values, and {@link Enum#name()} is used to convert enum values to
     * {@link String strings}.
     *
     * @param <E> The enum type
     * @param allowedValues The list of valid values
     * @return The created mapper
     */
    public static <E extends Enum<E>> EnumArgumentMapper<E> of(EnumSet<E> allowedValues) {
        return new EnumArgumentMapper<>(
                allowedValues,
                ResolutionStrategy.strict(),
                Enum::name
        );
    }

    /**
     * Constructs a new {@link Builder} instance.
     *
     * @param <E> The enum type
     * @return The created builder
     * @see Builder
     */
    public static <E extends Enum<E>> Builder<E> builder() {
        return new Builder<>();
    }

    @Override
    public E tryMap(CommandContext context, StringReader input) throws CommandException {
        String name = input.readSingle();
        for (E each : this.allowedValues) {
            if (this.resolutionStrategy.equals(name, each)) return each;
        }

        throw generateException(name);
    }

    @Override
    public List<String> complete(CommandContext context, String input) {
        return this.allowedValues.stream()
                .map(this.completionTransformer::transform)
                .toList();
    }

    /**
     * Controls how an enum value is detected.
     *
     * @param <E> The enum type
     */
    @FunctionalInterface
    public interface ResolutionStrategy<E extends Enum<E>> {

        /**
         * Checks, whether the input can be considered as being equals
         * to the provided candidate. If the returned value is true,
         * the mapper will return the value of candidate, otherwise
         * it will move to the next value.
         *
         * @param input The user input
         * @param candidate The enum value currently being tested
         * @return Whether they can be considered equal
         */
        boolean equals(String input, E candidate);

        /**
         * Returns a {@link ResolutionStrategy}, that is case-sensitive
         * (hence the name strict). This strategy is used by default.
         *
         * @param <E> The enum type
         * @return The created instance
         */
        static <E extends Enum<E>> ResolutionStrategy<E> strict() {
            return (input, candidate) -> input.equals(candidate.name());
        }

        /**
         * Returns a {@link ResolutionStrategy}, that is case-insensitive
         * (hence the name liberal).
         *
         * @param <E> The enum type
         * @return The created instance
         */
        static <E extends Enum<E>> ResolutionStrategy<E> liberal() {
            return (input, candidate) -> input.equalsIgnoreCase(candidate.name());
        }
    }

    /**
     * Controls the way an enum value is converted into a string.
     *
     * @param <E> The enum type
     */
    @FunctionalInterface
    public interface CompletionTransformer<E extends Enum<E>> {

        /**
         * Transforms the provided value into a string.
         *
         * @param value The value to transform
         * @return The string representation
         */
        String transform(E value);
    }

    /**
     * Builder to make {@link EnumArgumentMapper mappers} more configurable.
     *
     * @param <E> The enum type
     */
    public static final class Builder<E extends Enum<E>> {
        private EnumSet<E> allowedValues;
        private ResolutionStrategy<E> resolutionStrategy;
        private CompletionTransformer<E> completionTransformer;

        /**
         * Sets the allowed values that are going to be used by the mapper.
         *
         * @param allowedValues The values
         * @return This
         */
        public Builder<E> allow(EnumSet<E> allowedValues) {
            this.allowedValues = requireNonNull(allowedValues, "allowedValues cannot be null");
            return this;
        }

        /**
         * Shorthand for {@code allow(EnumSet.allOf(MyEnum.class))}.
         * @see this#allow(EnumSet)
         *
         * @return This
         */
        public Builder<E> allOf(Class<E> clazz) {
            return allow(EnumSet.allOf(clazz));
        }

        /**
         * Sets the {@link ResolutionStrategy} to be used by the mapper.
         *
         * @param resolutionStrategy The resolve strategy
         * @return This
         */
        public Builder<E> resolve(ResolutionStrategy<E> resolutionStrategy) {
            this.resolutionStrategy = requireNonNull(resolutionStrategy, "resolveStrategy cannot be null");
            return this;
        }

        /**
         * Sets the {@link CompletionTransformer} to be used by the mapper.
         *
         * @param completionTransformer The completion transformer
         * @return This
         */
        public Builder<E> complete(CompletionTransformer<E> completionTransformer) {
            this.completionTransformer = requireNonNull(completionTransformer, "completionTransformer cannot be null");
            return this;
        }

        /**
         * Creates the {@link EnumArgumentMapper} instance.
         *
         * @return The created mapper
         */
        public EnumArgumentMapper<E> build() {
            return new EnumArgumentMapper<>(this.allowedValues, this.resolutionStrategy, this.completionTransformer);
        }
    }
}
