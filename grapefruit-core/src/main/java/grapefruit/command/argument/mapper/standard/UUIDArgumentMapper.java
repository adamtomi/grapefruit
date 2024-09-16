package grapefruit.command.argument.mapper.standard;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgumentException;
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
        try {
            return UUID.fromString(input.readSingle());
        } catch (IllegalArgumentException ex) {
            throw new CommandArgumentException(); // TODO error message
        }
    }

    @Override
    public List<String> complete(CommandContext context, String input) {
        return List.of();
    }
}
