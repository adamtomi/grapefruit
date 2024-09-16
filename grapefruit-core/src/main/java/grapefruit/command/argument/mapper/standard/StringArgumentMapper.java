package grapefruit.command.argument.mapper.standard;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.List;

/**
 * Argument mapper implementation that maps user input into a {@link String}.
 */
public abstract class StringArgumentMapper implements ArgumentMapper<String> {

    /**
     * @return {@link Single#INSTANCE}
     * @see Single
     */
    public static StringArgumentMapper single() {
        return Single.INSTANCE;
    }

    /**
     * @return {@link Quotable#INSTANCE}
     * @see Quotable
     */
    public static StringArgumentMapper quotable() {
        return Quotable.INSTANCE;
    }

    /**
     * @return {@link Greedy#INSTANCE}
     * @see Greedy
     */
    public static StringArgumentMapper greedy() {
        return Greedy.INSTANCE;
    }


    @Override
    public List<String> complete(CommandContext context, String input) {
        // It's better not to provide completions for string arguments.
        return List.of();
    }

    /**
     * A {@link StringArgumentMapper} implementation, that will read a single string
     * from user input, and return it.
     */
    private static final class Single extends StringArgumentMapper {
        private static final Single INSTANCE = new Single();

        private Single() {}

        @Override
        public String tryMap(CommandContext context, StringReader input) throws CommandException {
            // Read just a single string
            return input.readSingle();
        }
    }

    /**
     * A {@link StringArgumentMapper} implementation, that either reads a single
     * string, or a string between quotation marks. Which mode the reading should
     * be carried out in depends on the first character of the user input. If it
     * happens to be {@link StringReader#SINGLE_QUOTE} or {@link StringReader#DOUBLE_QUOTE},
     * we are dealing with a quoted string and reading should be done accordingly,
     * otherwise just read a single string. <bold>NOTE:</bold> the opening and
     * closing quotation mark needs to be the same.
     */
    private static final class Quotable extends StringArgumentMapper {
        private static final Quotable INSTANCE = new Quotable();

        private Quotable() {}

        @Override
        public String tryMap(CommandContext context, StringReader input) throws CommandException {
            return input.readQuotable();
        }
    }

    /**
     * A {@link StringArgumentMapper} implementation, that will read the remaining
     * of the user input. Because of this behavior, greedy strings are expected to
     * be the last in an argument list.
     */
    private static final class Greedy extends StringArgumentMapper {
        private static final Greedy INSTANCE = new Greedy();

        private Greedy() {}

        @Override
        public String tryMap(CommandContext context, StringReader input) throws CommandException {
            return input.readRemaining();
        }
    }
}
