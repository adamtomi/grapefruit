package grapefruit.command.dispatcher.input;

import grapefruit.command.dispatcher.CommandSyntaxException;

import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

public class StringReaderImpl implements StringReader {
    private final String input;
    private int cursor;

    StringReaderImpl(String input) {
        this.input = requireNonNull(input, "input cannot be null");
    }

    @Override
    public boolean hasNext() {
        return this.cursor < this.input.length();
    }

    @Override
    public char next() {
        if (hasNext()) {
            return this.input.charAt(this.cursor++);
        }

        // TODO throw CommandSyntaxException(too few arguments)
        throw new NoSuchElementException("There is nothing left to read");
    }

    @Override
    public String readSingle() throws CommandSyntaxException {
        skipWhitespace();
        int start = this.cursor;
        readUntil(x -> !Character.isWhitespace(x));
        return this.input.substring(start, this.cursor);
    }

    @Override
    public String readQuotable() throws CommandSyntaxException {
        char next = next();
        if (next == SINGLE_QUOTE || next == DOUBLE_QUOTE) {
            int start = this.cursor;
            readUntil(x -> x == SINGLE_QUOTE || x == DOUBLE_QUOTE);
            return this.input.substring(start, this.cursor);
        }

        return readSingle();
    }

    @Override
    public String readRemaining() throws CommandSyntaxException {
        // TODO throw CommandSyntaxException instead
        if (!hasNext()) throw new NoSuchElementException("There is nothing to read");
        int start = this.cursor;
        this.cursor = this.input.length();
        return this.input.substring(start);
    }

    @Override
    public String consumed() {
        return this.input.substring(0, this.cursor - 1);
    }

    @Override
    public String unwrap() {
        return this.input;
    }

    private void readUntil(CharPredicate condition) {
        char c;
        do {
            c = next();
        } while (condition.test(c));
    }

    private void skipWhitespace() {
        readUntil(Character::isWhitespace);
    }

    @FunctionalInterface
    private interface CharPredicate {
        boolean test(char c);
    }
}
