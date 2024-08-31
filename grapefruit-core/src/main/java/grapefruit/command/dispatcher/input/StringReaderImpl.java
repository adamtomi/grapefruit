package grapefruit.command.dispatcher.input;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.StandardContextKeys;
import grapefruit.command.dispatcher.syntax.CommandSyntaxException;

import static java.util.Objects.requireNonNull;

public class StringReaderImpl implements StringReader {
    private final String input;
    private final CommandContext context;
    private int cursor;

    public StringReaderImpl(String input, CommandContext context) {
        this.input = requireNonNull(input, "input cannot be null");
        this.context = requireNonNull(context, "context cannot be null");
    }

    @Override
    public boolean hasNext() {
        return this.cursor < this.input.length();
    }

    @Override
    public char next() throws CommandSyntaxException {
        if (hasNext()) {
            return this.input.charAt(this.cursor++);
        }

        throw generateException();
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
        if (!hasNext()) throw generateException();
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

    private void readUntil(CharPredicate condition) throws CommandSyntaxException {
        char c;
        do {
            c = next();
        } while (condition.test(c));
    }

    private void skipWhitespace() throws CommandSyntaxException {
        readUntil(Character::isWhitespace);
    }

    private CommandSyntaxException generateException() {
        return CommandSyntaxException.from(
                this,
                this.context.get(StandardContextKeys.COMMAND_INSTANCE),
                CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS
        );
    }

    @FunctionalInterface
    private interface CharPredicate {
        boolean test(char c);
    }
}
