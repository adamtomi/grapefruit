package grapefruit.command.dispatcher.input;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.util.Range;
import grapefruit.command.util.function.CharPredicate;
import grapefruit.command.util.function.Function3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CommandInputTokenizerImpl implements CommandInputTokenizer.Internal {
    private final Deque<Range> consumed = new ArrayDeque<>();
    private final Unsafe unsafe;
    private final String input;
    private int cursor;

    public CommandInputTokenizerImpl(final String input) {
        this.input = requireNonNull(input, "input cannot be null");
        this.unsafe = new Unsafe(this);
    }

    @Override
    public String unwrap() {
        return this.input;
    }

    @Override
    public int cursor() {
        return this.cursor;
    }

    @Override
    public boolean canRead() {
        return this.cursor < this.input.length() - 1;
    }

    @Override
    public char read() throws MissingInputException {
        if (!canRead()) throw new MissingInputException();

        int from = this.cursor;
        this.cursor = from + 1;
        this.consumed.addLast(Range.range(from, this.cursor));
        return this.input.charAt(this.cursor);
    }

    @Override
    public char peek() {
        return isConsumed() ? 0 : this.input.charAt(this.cursor);
    }

    @Override // TODO remove new entries from this.consumedArgs
    public @Nullable String peekWord() {
        final int start = this.cursor;
        try {
            return readWord();
        } catch (final MissingInputException ex) {
            return null;
        } finally {
            this.cursor = start;
        }
    }

    @Override
    public String readWord() throws MissingInputException {
        skipWhitespace();
        return readWhileThrowOnEmpty(x -> !Character.isWhitespace(x));
    }

    @Override
    public String readQuotable() throws MissingInputException {
        skipWhitespace();
        final char start = peek();
        // This means we're dealing with a quoted string
        if (start == SINGLE_QUOTE || start == DOUBLE_QUOTE) {
            return performRead(() -> {
                this.cursor++; // Get rid of leading ("|')
                // Require the argument to be surrounded by the same kind of
                // quotation marks.
                final String result = readWhileThrowOnEmpty(x -> x != start);
                this.cursor++; // Get rid of trailing ("|')

                return result;
            });
        }

        return readWord();
    }

    @Override
    public String readRemaining() throws MissingInputException {
        skipWhitespace();
        return performRead(() -> {
            final int start = this.cursor;
            this.cursor = this.input.length();
            final String result = this.input.substring(start);
            if (result.isEmpty()) {
                throw new MissingInputException();
            }

            return result;
        });
    }

    @Override
    public @Nullable String peekRemaining() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String remainingOrEmpty() {
        try {
            return readRemaining();
        } catch (final MissingInputException ex) {
            return "";
        }
    }

    @Override
    public String consumed() {
        return this.input.substring(0, this.cursor);
    }

    @Override
    public CommandInputTokenizer.Unsafe unsafe() {
        return this.unsafe;
    }

    // An input is consumed if all arguments have been seen (and processed).
    private boolean isConsumed() {
        return this.cursor >= this.input.length();
    }

    private String readWhile(final CharPredicate condition) throws MissingInputException {
        if (isConsumed()) throw new MissingInputException();
        return performRead(() -> {
            final StringBuilder builder = new StringBuilder();
            char c;
            while (condition.test((c = peek()))) {
                builder.append(c);
                this.cursor++;
                if (isConsumed()) break;
            }

            return builder.toString();
        });
    }

    private String readWhileThrowOnEmpty(final CharPredicate condition) throws MissingInputException {
        final String result = readWhile(condition);
        if (result.isEmpty()) throw new MissingInputException();

        return result;
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

    private static final class Unsafe implements CommandInputTokenizer.Unsafe {
        private final CommandInputTokenizerImpl impl;

        private Unsafe(final CommandInputTokenizerImpl impl) {
            this.impl = requireNonNull(impl, "impl cannot be null");
        }

        @Override
        public void moveTo(final int position) {
            if (position < 0) {
                throw new IllegalArgumentException("Position cannot be negative");
            }

            this.impl.cursor = position;
        }

        @Override
        public Optional<String> lastConsumed() {
            final @Nullable Range range = this.impl.consumed.peekLast();
            return range == null
                    ? Optional.empty()
                    : Optional.of(this.impl.input.substring(range.from(), range.to()));
        }

        @Override
        public <X extends CommandArgumentException> X exception(final String argument, final Function3<String, String, String, X> provider) {
            // TODO
            return provider.apply(
                    this.impl.consumed(), // Consumed input
                    argument, // The argument that caused this exception
                    this.impl.remainingOrEmpty() // The remaining input TODO: don't use this method
            );
        }
    }
}
