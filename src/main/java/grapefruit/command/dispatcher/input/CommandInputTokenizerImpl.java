package grapefruit.command.dispatcher.input;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.Range;
import grapefruit.command.util.function.CharPredicate;
import grapefruit.command.util.function.Function3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CommandInputTokenizerImpl implements CommandInputTokenizer {
    private static final char SINGLE_QUOTE = '\'';
    private static final char DOUBLE_QUOTE = '"';
    private final Deque<Range> consumed = new ArrayDeque<>();
    private final Internal internal;
    private final String input;
    private int cursor;

    public CommandInputTokenizerImpl(final String input) {
        this.input = requireNonNull(input, "input cannot be null");
        this.internal = new Internal(this);
    }

    @Override
    public String input() {
        return this.input;
    }

    @Override
    public int cursor() {
        return this.cursor;
    }

    @Override
    public int length() {
        return this.input.length();
    }

    @Override
    public boolean canRead() {
        return this.cursor < this.input.length();
    }

    @Override
    public boolean canReadNonWhitespace() {
        if (!canRead()) return false;
        return this.input.substring(this.cursor).chars().anyMatch(x -> !Character.isWhitespace(x));
    }

    @Override
    public char read() throws MissingInputException {
        requireCanRead();

        int from = this.cursor;
        this.cursor = from + 1;
        this.consumed.addLast(Range.range(from, this.cursor));
        return this.input.charAt(from);
    }

    @Override
    public char peek() {
        // Do we want this?
        return canRead()
                ? this.input.charAt(this.cursor)
                : 0;
    }

    @Override
    public String peekWord() {
        final int start = this.cursor;
        try {
            return readWord();
        } catch (final MissingInputException ex) {
            return "";
        } finally {
            this.cursor = start;
        }
    }

    @Override
    public String readWord() throws MissingInputException {
        skipWhitespace();
        return readWhile(x -> !Character.isWhitespace(x));
    }

    @Override
    public String readQuotable() throws MissingInputException {
        skipWhitespace();
        final char start = peek();
        // This means we're dealing with a quoted string
        if (start == SINGLE_QUOTE || start == DOUBLE_QUOTE) {
            return performRead(() -> {
                read(); // Get rid of leading quotation
                // Require the argument to be surrounded by the same kind of
                // quotation marks.
                final String result = readWhile(x -> x != start);
                read(); // Get rid of trailing quotation

                return result;
            });
        }

        return readWord();
    }

    @Override
    public String readRemaining() throws MissingInputException {
        skipWhitespace();
        return performRead(() -> {
            final String result = remaining();
            this.cursor = this.input.length();
            if (result.isEmpty()) {
                throw new MissingInputException();
            }

            return result;
        });
    }

    @Override
    public String remaining() {
        return this.input.substring(this.cursor);
    }

    @Override
    public String consumed() {
        return this.input.substring(0, this.cursor);
    }

    @Override
    public Optional<String> lastConsumed() {
        final @Nullable Range range = this.consumed.peekLast();
        return range == null
                ? Optional.empty()
                : Optional.of(this.input.substring(range.from(), range.to()));
    }

    @Override
    public CommandInputTokenizer.Internal internal() {
        return this.internal;
    }

    private void requireCanRead() throws MissingInputException {
        if (!canRead()) throw new MissingInputException();
    }

    private String readWhile(final CharPredicate condition) throws MissingInputException {
        requireCanRead();
        return performRead(() -> {
            final StringBuilder builder = new StringBuilder();
            while (canRead() && condition.test(peek())) {
                builder.append(read());
            }

            return builder.toString();
        });
    }

    private void skipWhitespace() throws MissingInputException {
        readWhile(Character::isWhitespace);
    }

    private String performRead(final Read read) throws MissingInputException {
        final int from = this.cursor;
        final String result = read.perform();
        final int to = this.cursor;
        this.consumed.addLast(Range.range(from, to));
        return result;
    }

    @FunctionalInterface
    interface Read {

        String perform() throws MissingInputException;
    }

    private static final class Internal implements CommandInputTokenizer.Internal {
        private final CommandInputTokenizerImpl impl;

        private Internal(final CommandInputTokenizerImpl impl) {
            this.impl = requireNonNull(impl, "impl cannot be null");
        }

        @Override
        public <X extends CommandArgumentException> X gen(final String argument, final Function3<String, String, String, X> provider) {
            return provider.apply(
                    this.impl.consumed(), // Consumed input
                    argument, // The argument that caused this exception
                    this.impl.remaining() // The remaining input
            );
        }
    }
}
