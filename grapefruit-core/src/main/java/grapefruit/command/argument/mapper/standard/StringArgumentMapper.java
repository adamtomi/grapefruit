package grapefruit.command.argument.mapper.standard;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.List;

public abstract class StringArgumentMapper implements ArgumentMapper<String> {
    public static StringArgumentMapper single() {
        return Single.INSTANCE;
    }

    public static StringArgumentMapper quotable() {
        return Quotable.INSTANCE;
    }

    public static StringArgumentMapper greedy() {
        return Greedy.INSTANCE;
    }


    @Override
    public List<String> listSuggestions(CommandContext context, String input) {
        return List.of();
    }

    private static final class Single extends StringArgumentMapper {
        private static final Single INSTANCE = new Single();

        private Single() {}

        @Override
        public String tryMap(CommandContext context, StringReader input) throws CommandException {
            return input.readSingle();
        }
    }

    private static final class Quotable extends StringArgumentMapper {
        private static final Quotable INSTANCE = new Quotable();

        private Quotable() {}

        @Override
        public String tryMap(CommandContext context, StringReader input) throws CommandException {
            return input.readQuotable();
        }
    }

    private static final class Greedy extends StringArgumentMapper {
        private static final Greedy INSTANCE = new Greedy();

        private Greedy() {}

        @Override
        public String tryMap(CommandContext context, StringReader input) throws CommandException {
            return input.readRemaining();
        }
    }
}
