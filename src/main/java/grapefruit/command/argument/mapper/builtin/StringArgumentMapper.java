package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.CommandSyntaxException;
import grapefruit.command.dispatcher.input.MissingInputException;
import io.leangen.geantyref.TypeToken;

import java.io.Serial;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public abstract class StringArgumentMapper<S> extends AbstractArgumentMapper<S, String> {
    private static final TypeToken<String> TYPE = TypeToken.get(String.class);
    private final RegexHandler regexHandler;

    private StringArgumentMapper(final boolean isTerminal, final RegexHandler regexHandler) {
        super(TYPE, isTerminal);
        this.regexHandler = regexHandler;
    }

    public static <S> StringArgumentMapper<S> word() {
        return new Word<>(RegexHandler.nil());
    }

    public static <S> StringArgumentMapper<S> quotable() {
        return new Quotable<>(RegexHandler.nil());
    }

    public static <S> StringArgumentMapper<S> greedy() {
        return new Greedy<>(RegexHandler.nil());
    }

    public static <S> Builder<S> builder() {
        return new Builder<>();
    }

    @Override
    public final String tryMap(final CommandContext<S> context, final CommandInputTokenizer input) throws CommandException {
        final String value = extract(input);
        this.regexHandler.test(input, value);
        return value;
    }

    protected abstract String extract(final CommandInputTokenizer input) throws MissingInputException;

    @FunctionalInterface
    private interface RegexHandler {

        void test(final CommandInputTokenizer input, final String arg) throws CommandArgumentException;

        static RegexHandler nil() {
            return (input, arg) -> {};
        }

        static RegexHandler ofPattern(final Pattern pattern) {
            return ofPattern(pattern, (input, arg) ->
                    new UnfulfilledRegexException(input.consumed(), arg, input.remainingOrEmpty(), pattern)
            );
        }

        static RegexHandler ofPattern(
                final Pattern pattern,
                final BiFunction<CommandInputTokenizer, String, CommandArgumentException> exceptionFactory
        ) {
            return new PatternBackedRegexHandler(pattern, exceptionFactory);
        }
    }

    private static final class PatternBackedRegexHandler implements RegexHandler {
        private final Pattern pattern;
        private final BiFunction<CommandInputTokenizer, String, CommandArgumentException> exceptionFactory;

        private PatternBackedRegexHandler(
                final Pattern pattern,
                final BiFunction<CommandInputTokenizer, String, CommandArgumentException> exceptionFactory
        ) {
            this.pattern = requireNonNull(pattern, "pattern cannot be null");
            this.exceptionFactory = requireNonNull(exceptionFactory, "exceptionFactory cannot be null");
        }

        @Override
        public void test(final CommandInputTokenizer input, final String arg) throws CommandArgumentException {
            final Matcher matcher = this.pattern.matcher(arg);
            if (!matcher.matches()) {
                throw this.exceptionFactory.apply(input, arg);
            }
        }
    }

    public static final class UnfulfilledRegexException extends CommandArgumentException {
        @Serial
        private static final long serialVersionUID = 5332425347957824800L;
        private final Pattern pattern;

        public UnfulfilledRegexException(final String consumed, final String argument, final String remaining, final Pattern pattern) {
            super(consumed, argument, remaining);
            this.pattern = requireNonNull(pattern, "pattern cannot be null");
        }

        public Pattern pattern() {
            return this.pattern;
        }
    }

    private static final class Word<S> extends StringArgumentMapper<S> {

        private Word(final RegexHandler regexHandler) {
            super(false, regexHandler);
        }

        @Override
        protected String extract(final CommandInputTokenizer input) throws MissingInputException {
            return input.readWord();
        }
    }

    private static final class Quotable<S> extends StringArgumentMapper<S> {

        private Quotable(final RegexHandler regexHandler) {
            super(false, regexHandler);
        }

        @Override
        protected String extract(final CommandInputTokenizer input) throws MissingInputException {
            return input.readQuotable();
        }
    }

    private static final class Greedy<S> extends StringArgumentMapper<S> {

        private Greedy(final RegexHandler regexHandler) {
            super(true, regexHandler);
        }

        @Override
        protected String extract(final CommandInputTokenizer input) throws MissingInputException {
            return input.readRemaining();
        }
    }

    public static final class Builder<S> {
        private RegexHandler regexHandler = RegexHandler.nil();

        private Builder() {}

        public Builder<S> test(final Pattern pattern) {
            this.regexHandler = RegexHandler.ofPattern(pattern);
            return this;
        }

        public Builder<S> test(final Pattern pattern, final BiFunction<CommandInputTokenizer, String, CommandArgumentException> exceptionFactory) {
            this.regexHandler = RegexHandler.ofPattern(pattern, exceptionFactory);
            return this;
        }

        public Builder<S> test(final Pattern pattern, final Supplier<CommandArgumentException> exceptionFactory) {
            this.regexHandler = RegexHandler.ofPattern(pattern, (arg, input) -> exceptionFactory.get());
            return this;
        }

        public StringArgumentMapper<S> asWord() {
            return new Word<>(this.regexHandler);
        }

        public StringArgumentMapper<S> asQuotable() {
            return new Quotable<>(this.regexHandler);
        }

        public StringArgumentMapper<S> asGreedy() {
            return new Greedy<>(this.regexHandler);
        }
    }
}
