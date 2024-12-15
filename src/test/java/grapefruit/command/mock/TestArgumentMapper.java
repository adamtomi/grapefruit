package grapefruit.command.mock;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.AbstractArgumentMapper;
import grapefruit.command.argument.mapper.CommandInputAccess;
import grapefruit.command.dispatcher.CommandContext;
import io.leangen.geantyref.TypeToken;

import java.io.Serial;

public class TestArgumentMapper extends AbstractArgumentMapper<Object, String> {
    private final String expected;

    public TestArgumentMapper(final String expected) {
        super(TypeToken.get(String.class), false);
        this.expected = expected;
    }

    @Override
    public String tryMap(final CommandContext<Object> context, final CommandInputAccess access) throws CommandException {
        final String arg = access.input().readWord();
        if (!arg.equals(this.expected)) {
            throw access.generateFrom(new DummyException());
        }

        return arg;
    }

    private static final class DummyException extends CommandException {
        @Serial
        private static final long serialVersionUID = -4882128968203661422L;
    }
}
