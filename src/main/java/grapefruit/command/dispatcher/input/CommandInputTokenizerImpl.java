package grapefruit.command.dispatcher.input;

import grapefruit.command.util.function.CharPredicate;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

final class CommandInputTokenizerImpl implements CommandInputTokenizer {
    private final String input;
    private int cursor;

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
        if (hasNext()) {
            return this.input.charAt(++this.cursor);
        }

        throw generateException();
    }

    @Override
    public char peek() {
        return this.input.charAt(this.cursor);
    }

    @Override
    public void advance() throws CommandSyntaxException {
        if (hasNext()) {
            this.cursor++;
        } else {
            throw generateException();
        }
    }

    @Override
    public @Nullable String peekWord() {
        final int start = this.cursor;
        try {
            return readWord();
        } catch (CommandSyntaxException ex) {
            return null;
        } finally {
            this.cursor = start;
        }
    }

    @Override
    public String readWord() throws CommandSyntaxException {
        skipWhitespace();
        return readWhile(x -> !Character.isWhitespace(x));
    }

    @Override
    public String readQuotable() throws CommandSyntaxException {
        skipWhitespace();
        final char next = peek();
        // This means we're dealing with a quoted string
        if (next == SINGLE_QUOTE || next == DOUBLE_QUOTE) {
            next(); // Get rid of leading ("|')
            int start = this.cursor;
            // Require the argument to be surrounded by the same kind of
            // quotation marks.
            readWhile(x -> x != next);
            final String result = this.input.substring(start, this.cursor);
            next(); // Get rid of trailing ("|')
            return result;
        }

        return readWord();
    }

    @Override
    public String readRemaining() throws CommandSyntaxException {
        if (!hasNext()) throw generateException();
        final int start = this.cursor;
        this.cursor = this.input.length();
        return this.input.substring(start);
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

    private String readWhile(final CharPredicate condition) throws CommandSyntaxException {
        if (!hasNext()) throw generateException();
        final StringBuilder builder = new StringBuilder();
        char c;
        while (condition.test((c = peek()))) {
            builder.append(c);
            if (hasNext()) {
                advance();
            } else {
                break;
            }
        }

        return builder.toString();
    }

    private void skipWhitespace() throws CommandSyntaxException {
        readWhile(Character::isWhitespace);
    }

    private static CommandSyntaxException generateException() {
        return new CommandSyntaxException(null, CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS);
    }
}
