package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * An {@link ArgumentMapper} implementation that converts strings into numbers.
 *
 * @param <N> The number type
 */
public final class NumericArgumentMapper<N extends Number> implements ArgumentMapper<N> {
    /** The internal mapper is used for the actual conversion. */
    private final Function<String, N> internalMapper;

    private NumericArgumentMapper(Function<String, N> internalMapper) {
        this.internalMapper = requireNonNull(internalMapper, "internalMapper cannot be null");
    }

    /**
     * @return {@link ArgumentMapper} mapping strings into bytes.
     */
    public static NumericArgumentMapper<Byte> byteMapper() {
        return new NumericArgumentMapper<>(Byte::parseByte);
    }

    /**
     * @return {@link ArgumentMapper} mapping strings into shorts.
     */
    public static NumericArgumentMapper<Short> shortMapper() {
        return new NumericArgumentMapper<>(Short::parseShort);
    }

    /**
     * @return {@link ArgumentMapper} mapping strings into ints.
     */
    public static NumericArgumentMapper<Integer> intMapper() {
        return new NumericArgumentMapper<>(Integer::parseInt);
    }

    /**
     * @return {@link ArgumentMapper} mapping strings into longs.
     */
    public static NumericArgumentMapper<Long> longMapper() {
        return new NumericArgumentMapper<>(Long::parseLong);
    }

    /**
     * @return {@link ArgumentMapper} mapping strings into floats.
     */
    public static NumericArgumentMapper<Float> floatMapper() {
        return new NumericArgumentMapper<>(Float::parseFloat);
    }

    /**
     * @return {@link ArgumentMapper} mapping strings into doubles.
     */
    public static NumericArgumentMapper<Double> doubleMapper() {
        return new NumericArgumentMapper<>(Double::parseDouble);
    }

    @Override
    public N tryMap(CommandContext context, StringReader input) throws CommandException {
        String value = input.readSingle();
        try {
            return this.internalMapper.apply(value);
        } catch (NumberFormatException ex) {
            throw new CommandArgumentException(); // TODO error message
        }
    }

    @Override
    public List<String> complete(CommandContext context, String input) {
        // TODO not sure, if we want completions. Figure out if we do, and implement it, if the answer happens to be yes.
        return List.of();
    }
}
