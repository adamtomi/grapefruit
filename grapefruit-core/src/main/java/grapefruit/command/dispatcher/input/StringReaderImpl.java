package grapefruit.command.dispatcher.input;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.StandardContextKeys;
import grapefruit.command.dispatcher.syntax.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;

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
        if (hasNext()) return this.input.charAt(this.cursor++);

        throw generateException();
    }

    private char peek() {
        if (hasNext()) return this.input.charAt(this.cursor);

        return ' ';
    }

    @Override
    public @Nullable String peekSingle() {
        int start = this.cursor;
        try {
            return readSingle();
        } catch (CommandSyntaxException ex) {
            return null;
        } finally {
            this.cursor = start;
        }
    }

    @Override
    public String readSingle() throws CommandSyntaxException {
        skipWhitespace();
        int start = this.cursor;
        readWhile(x -> !Character.isWhitespace(x) && hasNext());
        return this.input.substring(start, this.cursor);
    }

    @Override
    public String readQuotable() throws CommandSyntaxException {
        skipWhitespace();
        char next = peek();
        // This means we're dealing with a quoted string
        if (next == SINGLE_QUOTE || next == DOUBLE_QUOTE) {
            next(); // Get rid of leading ("|')
            int start = this.cursor;
            readWhile(x -> x != SINGLE_QUOTE && x != DOUBLE_QUOTE);
            String result = this.input.substring(start, this.cursor);
            next(); // Get rid of trailing ("|')
            return result;
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
        return this.input.substring(0, this.cursor);
    }

    @Override
    public String unwrap() {
        return this.input;
    }

    private void readWhile(CharPredicate condition) throws CommandSyntaxException {
        while (condition.test(peek())) next();
    }

    private void skipWhitespace() throws CommandSyntaxException {
        readWhile(Character::isWhitespace);
    }

    private CommandSyntaxException generateException() {
        return CommandSyntaxException.from(
                this,
                this.context.getSafe(StandardContextKeys.COMMAND_INSTANCE).orElse(null),
                CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS
        );
    }

    @FunctionalInterface
    private interface CharPredicate {
        boolean test(char c);
    }
}
