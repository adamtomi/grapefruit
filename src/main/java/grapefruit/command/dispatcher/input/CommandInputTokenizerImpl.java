package grapefruit.command.dispatcher.input;

import grapefruit.command.util.function.CharPredicate;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

final class CommandInputTokenizerImpl implements CommandInputTokenizer {
    private final String input;
    private int cursor;
    private boolean consumed;

    public CommandInputTokenizerImpl(final String input) {
        this.input = requireNonNull(input, "input cannot be null");
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
    public void moveTo(final int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Position cannot be negative");
        }

        this.cursor = position;
        this.consumed = this.cursor >= this.input.length();
    }

    @Override
    public boolean hasNext() {
        return this.cursor < this.input.length() - 1;
    }

    @Override
    public char next() throws MissingInputException {
        checkConsumed();
        if (hasNext()) {
            return this.input.charAt(++this.cursor);
        }

        throw new MissingInputException();
    }

    @Override
    public char peek() {
        return this.consumed
                ? 0
                : this.input.charAt(this.cursor);
    }

    @Override
    public void advance() throws MissingInputException {
        checkConsumed();
        if (this.cursor < this.input.length()) {
            this.cursor++;
            if (this.cursor >= this.input.length()) {
                this.consumed = true;
            }

        } else {
            throw new MissingInputException();
        }
    }

    @Override
    public @Nullable String peekWord() {
        final int start = this.cursor;
        final boolean consumed = this.consumed;
        try {
            return readWord();
        } catch (final MissingInputException ex) {
            return null;
        } finally {
            this.cursor = start;
            this.consumed = consumed;
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
            advance(); // Get rid of leading ("|')
            // Require the argument to be surrounded by the same kind of
            // quotation marks.
            final String result = readWhileThrowOnEmpty(x -> x != start);
            advance(); // Get rid of trailing ("|')

            return result;
        }

        return readWord();
    }

    @Override
    public String readRemaining() throws MissingInputException {
        skipWhitespace();
        final int start = this.cursor;
        this.cursor = this.input.length();
        final String result = this.input.substring(start);
        if (result.isEmpty()) {
            throw new MissingInputException();
        }

        return result;
    }

    @Override
    public String consumed() {
        return this.input.substring(0, this.cursor);
    }

    @Override
    public String remainingOrEmpty() {
        try {
            return readRemaining();
        } catch (final MissingInputException ex) {
            return "";
        }
    }

    private void checkConsumed() throws MissingInputException {
        if (this.consumed) throw new MissingInputException();
    }

    private String readWhile(final CharPredicate condition) throws MissingInputException {
        checkConsumed();
        if (this.cursor >= this.input.length()) {
            throw new MissingInputException();
        }

        final StringBuilder builder = new StringBuilder();
        char c;
        while (condition.test((c = peek()))) {
            builder.append(c);
            if (this.cursor < this.input.length()) {
                advance();
                if (this.consumed) {
                    break;
                }
            } else {
                break;
            }
        }

        return builder.toString();
    }

    private String readWhileThrowOnEmpty(final CharPredicate condition) throws MissingInputException {
        final String result = readWhile(condition);
        if (result.isEmpty()) throw new MissingInputException();

        return result;
    }

    private void skipWhitespace() throws MissingInputException {
        readWhile(Character::isWhitespace);
    }
}
