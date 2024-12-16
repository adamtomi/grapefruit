package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.argument.mapper.ArgumentMappingException;
import grapefruit.command.argument.mapper.CommandInputAccess;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.MissingInputException;

import java.io.Serial;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

// TODO completions
public final class NumericArgumentMapper<S, N extends Number> extends AbstractArgumentMapper<S, N> {
    private final Function<String, N> internalMapper;
    private final Supplier<CommandException> exceptionSupplier;

    private NumericArgumentMapper(final Class<N> type, final Function<String, N> internalMapper, final Supplier<CommandException> exceptionSupplier) {
        super(type, false);
        this.internalMapper = requireNonNull(internalMapper, "internalMapper cannot be null");
        this.exceptionSupplier = requireNonNull(exceptionSupplier, "exceptionSupplier cannot be null");
    }

    public static <S> NumericArgumentMapper<S, Byte> byteMapper() {
        return byteMapper(NumberMappingException::new);
    }

    public static <S> NumericArgumentMapper<S, Byte> byteMapper(final Supplier<CommandException> exceptionSupplier) {
        return new NumericArgumentMapper<>(Byte.class, Byte::parseByte, exceptionSupplier);
    }

    public static <S> NumericArgumentMapper<S, Short> shortMapper() {
        return shortMapper(NumberMappingException::new);
    }

    public static <S> NumericArgumentMapper<S, Short> shortMapper(final Supplier<CommandException> exceptionSupplier) {
        return new NumericArgumentMapper<>(Short.class, Short::parseShort, exceptionSupplier);
    }

    public static <S> NumericArgumentMapper<S, Integer> intMapper() {
        return intMapper(NumberMappingException::new);
    }

    public static <S> NumericArgumentMapper<S, Integer> intMapper(final Supplier<CommandException> exceptionSupplier) {
        return new NumericArgumentMapper<>(Integer.class, Integer::parseInt, exceptionSupplier);
    }

    public static <S> NumericArgumentMapper<S, Long> longMapper() {
        return longMapper(NumberMappingException::new);
    }

    public static <S> NumericArgumentMapper<S, Long> longMapper(final Supplier<CommandException> exceptionSupplier) {
        return new NumericArgumentMapper<>(Long.class, Long::parseLong, exceptionSupplier);
    }

    public static <S> NumericArgumentMapper<S, Float> floatMapper() {
        return floatMapper(NumberMappingException::new);
    }

    public static <S> NumericArgumentMapper<S, Float> floatMapper(final Supplier<CommandException> exceptionSupplier) {
        return new NumericArgumentMapper<>(Float.class, Float::parseFloat, exceptionSupplier);
    }

    public static <S> NumericArgumentMapper<S, Double> doubleMapper() {
        return doubleMapper(NumberMappingException::new);
    }

    public static <S> NumericArgumentMapper<S, Double> doubleMapper(final Supplier<CommandException> exceptionSupplier) {
        return new NumericArgumentMapper<>(Double.class, Double::parseDouble, exceptionSupplier);
    }

    @Override
    public N tryMap(final CommandContext<S> context, final CommandInputAccess access) throws ArgumentMappingException, MissingInputException {
        try {
            return this.internalMapper.apply(access.input().readWord());
        } catch (final NumberFormatException ex) {
            throw access.generateFrom(this.exceptionSupplier.get());
        }
    }

    public static final class NumberMappingException extends CommandException {
        @Serial
        private static final long serialVersionUID = 686300879299755230L;

        public NumberMappingException() {
            super();
        }
    }
}
