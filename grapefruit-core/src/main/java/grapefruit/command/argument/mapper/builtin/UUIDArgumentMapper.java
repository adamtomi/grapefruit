package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.List;
import java.util.UUID;

/**
 * An {@link ArgumentMapper} implementation that converts strings into
 * {@link UUID uuids}.
 */
public class UUIDArgumentMapper implements ArgumentMapper<UUID> {

    @Override
    public UUID tryMap(CommandContext context, StringReader input) throws CommandException {
        String value = input.readSingle();
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw generateException(value);
        }
    }

    @Override
    public List<String> complete(CommandContext context, String input) {
        return List.of();
    }
}
