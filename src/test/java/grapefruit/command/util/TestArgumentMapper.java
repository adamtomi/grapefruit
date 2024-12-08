package grapefruit.command.util;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import io.leangen.geantyref.TypeToken;

public class TestArgumentMapper extends AbstractArgumentMapper<Object, String> {
    private final String expected;

    public TestArgumentMapper(final String expected) {
        super(TypeToken.get(String.class), false);
        this.expected = expected;
    }

    @Override
    public String tryMap(final CommandContext<Object> context, final CommandInputTokenizer input) throws CommandException {
        final String arg = input.readWord();
        if (!arg.equals(this.expected)) {
            throw new CommandArgumentException(input.consumed(), arg, input.remainingOrEmpty());
        }

        return arg;
    }
}
