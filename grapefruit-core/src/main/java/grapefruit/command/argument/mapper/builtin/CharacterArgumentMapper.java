package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.List;

/**
 * An {@link ArgumentMapper} implementation that converts strings into characters.
 */
public class CharacterArgumentMapper implements ArgumentMapper<Character> {

    @Override
    public Character tryMap(CommandContext context, StringReader input) throws CommandException {
        String value = input.readSingle();
        // Input too long, throw an error
        if (value.length() != 1) throw new CommandArgumentException(); // TODO error message

        return value.charAt(0);
    }

    @Override
    public List<String> complete(CommandContext context, String input) {
        // We don't want to suggest anything
        return List.of();
    }
}
