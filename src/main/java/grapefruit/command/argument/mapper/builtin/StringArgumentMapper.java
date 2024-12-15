package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.argument.mapper.CommandInputAccess;
import grapefruit.command.dispatcher.CommandContext;

import java.io.Serial;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public abstract class StringArgumentMapper<S> extends AbstractArgumentMapper<S, String> {

    protected StringArgumentMapper(final boolean isTerminal) {
        super(String.class, isTerminal);
    }

    public static <S> StringArgumentMapper<S> word() {
        return new Word<>();
    }

    public static <S> StringArgumentMapper<S> quotable() {
        return new Quotable<>();
    }

    public static <S> StringArgumentMapper<S> greedy() {
        return new Greedy<>();
    }

    public static <S> Filter<S, String> regex(final Pattern pattern, final Filter.ExceptionFactory<S, String> exceptionFactory) {
        return new Regex<>(pattern, exceptionFactory);
    }

    public static <S> Filter<S, String> regex(final Pattern pattern, final Supplier<CommandException> supplier) {
        return regex(pattern, Filter.ExceptionFactory.contextFree(supplier));
    }

    public static <S> Filter<S, String> regex(final Pattern pattern) {
        return regex(pattern, () -> new RegexException(pattern));
    }

    private static final class Word<S> extends StringArgumentMapper<S> {
        private Word() {
            super(false);
        }

        @Override
        public String tryMap(final CommandContext<S> context, final CommandInputAccess access) throws CommandException {
            return access.input().readWord();
        }
    }

    private static final class Quotable<S> extends StringArgumentMapper<S> {
        private Quotable() {
            super(false);
        }

        @Override
        public String tryMap(final CommandContext<S> context, final CommandInputAccess access) throws CommandException {
            return access.input().readQuotable();
        }
    }

    private static final class Greedy<S> extends StringArgumentMapper<S> {
        private Greedy() {
            super(true);
        }

        @Override
        public String tryMap(final CommandContext<S> context, final CommandInputAccess access) throws CommandException {
            return access.input().readRemaining();
        }
    }

    private static final class Regex<S> implements ArgumentMapper.Filter<S, String> {
        private final Pattern pattern;
        private final ExceptionFactory<S, String> exceptionFactory;

        Regex(final Pattern pattern, final ExceptionFactory<S, String> exceptionFactory) {
            this.pattern = requireNonNull(pattern, "pattern cannot be null");
            this.exceptionFactory = requireNonNull(exceptionFactory, "exceptionFactory cannot be null");
        }

        @Override
        public boolean test(final CommandContext<S> context, final String value) {
            final Matcher matcher = this.pattern.matcher(value);
            return matcher.matches();
        }

        @Override
        public CommandException generateException(final CommandContext<S> context, final String value) {
            return this.exceptionFactory.create(context, value);
        }
    }

    public static final class RegexException extends CommandException {
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
