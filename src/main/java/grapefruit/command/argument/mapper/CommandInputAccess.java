package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;

public interface CommandInputAccess {

    CommandInputTokenizer input();

    ArgumentMappingException wrapException(final CommandException cause);

    String consumedInput();

    static CommandInputAccess wrap(final CommandInputTokenizer input) {
        return new CommandInputAccessImpl(input);
    }
}
