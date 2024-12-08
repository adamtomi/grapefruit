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
    public boolean hasNext() {
        return this.cursor < this.input.length() - 1;
    }

    @Override
    public char next() throws CommandSyntaxException {
        checkConsumed();
        if (hasNext()) {
            return this.input.charAt(++this.cursor);
        }

        throw generateException();
    }

    @Override
    public char peek() {
        return this.consumed
                ? 0
                : this.input.charAt(this.cursor);
    }

    @Override
    public void advance() throws CommandSyntaxException {
        checkConsumed();
        if (this.cursor < this.input.length()) {
            this.cursor++;
            if (this.cursor >= this.input.length()) {
                this.consumed = true;
            }

        } else {
            throw generateException();
        }
    }

    @Override
    public @Nullable String peekWord() {
        final int start = this.cursor;
        final boolean consumed = this.consumed;
        try {
            return readWord();
        } catch (CommandSyntaxException ex) {
            return null;
        } finally {
            this.cursor = start;
            this.consumed = consumed;
        }
    }

    @Override
    public String readWord() throws CommandSyntaxException {
        skipWhitespace();
        return readWhileThrowOnEmpty(x -> !Character.isWhitespace(x));
    }

    @Override
    public String readQuotable() throws CommandSyntaxException {
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
    public String readRemaining() throws CommandSyntaxException {
        skipWhitespace();
        final int start = this.cursor;
        this.cursor = this.input.length();
        final String result = this.input.substring(start);
        if (result.isEmpty()) {
            throw generateException();
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
        } catch (final CommandSyntaxException ex) {
            return "";
        }
    }

    private void checkConsumed() throws CommandSyntaxException {
        if (this.consumed) throw generateException();
    }

    private String readWhile(final CharPredicate condition) throws CommandSyntaxException {
        checkConsumed();
        if (this.cursor >= this.input.length()) {
            throw generateException();
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

    private String readWhileThrowOnEmpty(final CharPredicate condition) throws CommandSyntaxException {
        final String result = readWhile(condition);
        if (result.isEmpty()) throw generateException();

        return result;
    }

    private void skipWhitespace() throws CommandSyntaxException {
        readWhile(Character::isWhitespace);
    }

    private static CommandSyntaxException generateException() {
        return new CommandSyntaxException(null, CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS);
    }
}
