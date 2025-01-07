package grapefruit.command.mock;

import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.argument.mapper.ArgumentMappingException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;
import io.leangen.geantyref.TypeToken;

public class TestArgumentMapper extends AbstractArgumentMapper<Object, String> {
    private final String expected;

    public TestArgumentMapper(final String expected) {
        super(TypeToken.get(String.class), false);
        this.expected = expected;
    }

    @Override
    public String tryMap(final CommandContext<Object> context, final CommandInputTokenizer input) throws ArgumentMappingException, MissingInputException {
        final String arg = input.readWord();
        if (!arg.equals(this.expected)) {
            throw new ArgumentMappingException();
        }

        return arg;
    }
}
