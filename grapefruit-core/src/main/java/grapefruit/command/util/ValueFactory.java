package grapefruit.command.util;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;

import static grapefruit.command.dispatcher.InternalContextKeys.INPUT;
import static java.util.Objects.requireNonNull;


/**
 * Computes a value based on some context.
 *
 * @param <T> The expeced type of the value
 */
@FunctionalInterface
public interface ValueFactory<T> {

    /**
     * Computes and returns a value computed based
     * on the provided context.
     *
     * @param context The context to calculate the value from
     * @return The computed value
     * @throws CommandException If there is an error during
     * calculation
     */
    T compute(CommandContext context) throws CommandException;

    /**
     * Returns a {@link ValueFactory} instance that always
     * returns the provided value.
     *
     * @param <T> The expected value type
     * @param value The value
     * @return The created factory
     */
    static <T> ValueFactory<T> constant(T value) {
        return context -> value;
    }

    /**
     * Returns a {@link ValueFactory} instance that computes
     * the value by invoking the supplied {@link ArgumentMapper}
     * instance.
     *
     * @param <T> The expected value type
     * @param mapper The mapper to invoke
     * @return The created factory
     */
    static <T> ValueFactory<T> mapBy(ArgumentMapper<T> mapper) {
        return new MapperBacked<>(mapper);
    }

    final class MapperBacked<T> implements ValueFactory<T> {
        private final ArgumentMapper<T> argumentMapper;

        private MapperBacked(ArgumentMapper<T> argumentMapper) {
            this.argumentMapper = requireNonNull(argumentMapper, "argumentMapper");
        }

        @Override
        public T compute(CommandContext context) throws CommandException {
            return this.argumentMapper.tryMap(context, context.require(INPUT));
        }
    }
}
