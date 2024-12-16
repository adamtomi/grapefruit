package grapefruit.command.runtime.argument.mapper.builtin;

import grapefruit.command.runtime.CommandException;
import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.dispatcher.CommandContext;
import grapefruit.command.runtime.dispatcher.input.StringReader;

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
