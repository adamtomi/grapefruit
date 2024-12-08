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
        return this.input.charAt(this.cursor);
    }

    @Override
    public void advance() throws CommandSyntaxException {
        checkConsumed();
        if (this.cursor < this.input.length()) {
            this.cursor++;
            System.out.println("advance: %d, %d".formatted(this.cursor, this.input.length()));
            if (this.cursor >= this.input.length()) {
                System.out.println("mark as consumed");
                this.consumed = true;
            }

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
        return readWhile(x -> !Character.isWhitespace(x), true);
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
            final String result = readWhile(x -> x != start, true);
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

    private String readWhile(final CharPredicate condition, final boolean throwOnEmpty) throws CommandSyntaxException {
        checkConsumed();
        System.out.println("--------------- readwhile(%d / %d) ---------------".formatted(this.cursor, this.input.length()));
        // this.cursor < this.input.length()
        if (/* !hasNext() */this.cursor >= this.input.length()) {
            System.out.println("cannot read char at current index");
            throw generateException();
        }

        final StringBuilder builder = new StringBuilder();
        char c;
        System.out.println("starting loop");
        while (condition.test((c = peek()))) {
            System.out.println(c);
            builder.append(c);
            if (this.cursor < this.input.length()) {
                System.out.println("hasNext, advance()");
                advance();
                System.out.println("done");
                if (this.consumed) {
                    System.out.println("consumed, out");
                    break;
                }
            } else {
                System.out.println("nothing to read");
                break;
            }
        }

        if (builder.isEmpty() && throwOnEmpty) {
            System.out.println("builder is empty");
            // Couldn't read anything
            throw generateException();
        }

        System.out.println("return");
        final String result = builder.toString();
        System.out.println(result);
        return result;
    }

    private void skipWhitespace() throws CommandSyntaxException {
        readWhile(Character::isWhitespace, false);
    }

    private static CommandSyntaxException generateException() {
        return new CommandSyntaxException(null, CommandSyntaxException.Reason.TOO_FEW_ARGUMENTS);
    }
}
