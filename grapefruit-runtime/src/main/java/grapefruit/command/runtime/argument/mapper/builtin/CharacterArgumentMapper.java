package grapefruit.command.runtime.argument.mapper.builtin;

import grapefruit.command.runtime.CommandException;
import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.dispatcher.CommandContext;
import grapefruit.command.runtime.dispatcher.input.StringReader;

import java.util.List;

/**
 * An {@link ArgumentMapper} implementation that converts strings into characters.
 */
public class CharacterArgumentMapper implements ArgumentMapper<Character> {

    @Override
    public Character tryMap(CommandContext context, StringReader input) throws CommandException {
        String value = input.readSingle();
        // Input too long, throw an error
        if (value.length() != 1) throw generateException(value);

        return value.charAt(0);
    }

    @Override
    public List<String> complete(CommandContext context, String input) {
        // We don't want to suggest anything
        return List.of();
    }
}
