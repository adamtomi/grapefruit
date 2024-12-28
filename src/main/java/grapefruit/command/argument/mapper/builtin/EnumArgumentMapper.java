package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.argument.mapper.ArgumentMappingException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class EnumArgumentMapper<S, E extends Enum<E>> extends AbstractArgumentMapper<S, E> {
    private final Class<E> type;
    private final EnumResolver<E> resolver;
    private final Supplier<ArgumentMappingException> exceptionSupplier;

    private EnumArgumentMapper(final Class<E> type, final EnumResolver<E> resolver, final Supplier<ArgumentMappingException> exceptionSupplier) {
        super(type, false);
        this.type = requireNonNull(type, "type cannot be null");
        this.resolver = requireNonNull(resolver, "resolver cannot be null");
        this.exceptionSupplier = requireNonNull(exceptionSupplier, "exceptionSupplier cannot be null");
    }

    public static <S, E extends Enum<E>> EnumArgumentMapper<S, E> strict(final Class<E> type, final Supplier<ArgumentMappingException> exceptionSupplier) {
        return new EnumArgumentMapper<>(type, EnumResolver.strict(), exceptionSupplier);
    }

    public static <S, E extends Enum<E>> EnumArgumentMapper<S, E> strict(final Class<E> type) {
        return strict(type, () -> new EnumMappingException(type));
    }

    public static <S, E extends Enum<E>> EnumArgumentMapper<S, E> lenient(final Class<E> type, final Supplier<ArgumentMappingException> exceptionSupplier) {
        return new EnumArgumentMapper<>(type, EnumResolver.lenient(), exceptionSupplier);
    }

    public static <S, E extends Enum<E>> EnumArgumentMapper<S, E> lenient(final Class<E> type) {
        return lenient(type, () -> new EnumMappingException(type));
    }

    @Override
    public E tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws ArgumentMappingException, MissingInputException {
        final String value = input.readWord();
        for (final E e : this.type.getEnumConstants()) {
            if (this.resolver.matches(e, value)) return e;
        }

        throw this.exceptionSupplier.get();
    }

    @Override
    public List<String> complete(final CommandContext<S> context, final String input) {
        return Arrays.stream(this.type.getEnumConstants())
                .map(this.resolver::complete)
                .toList();
    }

    private interface EnumResolver<E extends Enum<E>> {

        boolean matches(final E candidate, final String input);

        String complete(final E value);

        static <E extends Enum<E>> EnumResolver<E> strict() {
            return new EnumResolverImpl<>((candidate, value) -> candidate.name().equals(value), Enum::name);
        }

        static <E extends Enum<E>> EnumResolver<E> lenient() {
            return new EnumResolverImpl<>((candidate, value) -> candidate.name().equalsIgnoreCase(value), x -> x.name().toLowerCase());
        }
    }

    private static final class EnumResolverImpl<E extends Enum<E>> implements EnumResolver<E> {
        private final BiPredicate<E, String> matcher;
        private final Function<E, String> completer;

        private EnumResolverImpl(final BiPredicate<E, String> matcher, final Function<E, String> completer) {
            this.matcher = matcher;
            this.completer = completer;
        }

        @Override
        public boolean matches(final E candidate, final String input) {
            return this.matcher.test(candidate, input);
        }

        @Override
        public String complete(final E value) {
            return this.completer.apply(value);
        }
    }

    public static final class EnumMappingException extends ArgumentMappingException {
        @Serial
        private static final long serialVersionUID = -5281645874422380564L;
        private final Class<? extends Enum<?>> type;

        public EnumMappingException(final Class<? extends Enum<?>> type) {
            this.type = requireNonNull(type, "type cannot be null");
        }

        public Class<? extends Enum<?>> type() {
            return this.type;
        }
    }
}
