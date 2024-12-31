package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.argument.mapper.ArgumentMappingException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;
import grapefruit.command.util.function.CheckedFunction;

import java.io.Serial;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public final class StringArgumentMapper<S> extends AbstractArgumentMapper<S, String> {
    private final CheckedFunction<CommandInputTokenizer, String, MissingInputException> internalMapper;

    private StringArgumentMapper(
            final boolean isTerminal,
            final CheckedFunction<CommandInputTokenizer, String, MissingInputException> internalMapper
    ) {
        super(String.class, isTerminal);
        this.internalMapper = requireNonNull(internalMapper, "internalMapper cannot be null");
    }

    public static <S> StringArgumentMapper<S> word() {
        return new StringArgumentMapper<>(false, CommandInputTokenizer::readWord);
    }

    public static <S> StringArgumentMapper<S> quotable() {
        return new StringArgumentMapper<>(false, CommandInputTokenizer::readQuotable);
    }

    public static <S> StringArgumentMapper<S> greedy() {
        return new StringArgumentMapper<>(true, CommandInputTokenizer::readRemaining);
    }

    public static <S> Filter<S, String> regex(
            final Pattern pattern,
            final ArgumentMappingException.Factory<String> exceptionFactory
    ) {
        return new Regex<>(pattern, exceptionFactory);
    }

    public static <S> Filter<S, String> regex(final Pattern pattern, final Supplier<ArgumentMappingException> supplier) {
        return regex(pattern, (context, value) -> supplier.get());
    }

    public static <S> Filter<S, String> regex(final Pattern pattern) {
        return regex(pattern, () -> new RegexException(pattern));
    }

    @Override
    public String tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws MissingInputException {
        return this.internalMapper.apply(input);
    }

    private static final class Regex<S> implements ArgumentMapper.Filter<S, String> {
        private final Pattern pattern;
        private final ArgumentMappingException.Factory<String> exceptionFactory;

        Regex(final Pattern pattern, final ArgumentMappingException.Factory<String> exceptionFactory) {
            this.pattern = requireNonNull(pattern, "pattern cannot be null");
            this.exceptionFactory = requireNonNull(exceptionFactory, "exceptionFactory cannot be null");
        }

        @Override
        public void test(final CommandContext<S> context, final String value) throws ArgumentMappingException {
            final Matcher matcher = this.pattern.matcher(value);
            if (!matcher.matches()) {
                throw this.exceptionFactory.create(context, value);
            }
        }
    }

    public static final class RegexException extends ArgumentMappingException {
        @Serial
        private static final long serialVersionUID = -2684795575952022226L;
        private final Pattern pattern;

        public RegexException(final Pattern pattern) {
            this.pattern = requireNonNull(pattern, "pattern cannot be null");
        }

        public Pattern pattern() {
            return this.pattern;
        }
    }
}
