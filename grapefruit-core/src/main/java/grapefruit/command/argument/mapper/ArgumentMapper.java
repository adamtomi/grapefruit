package grapefruit.command.argument.mapper;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.List;

/**
 * Argument mappers are responsible for parsing data from its
 * {@link String} representation into other data types.
 *
 * @param <T> The other data type
 */
public interface ArgumentMapper<T> {

    /**
     * Maps user input from its {@link String} representation
     * into some other data type.
     *
     * @param context The current command context
     * @param input The reader reading user input
     * @return The mapped data
     * @throws CommandException If the reader fails
     * or the provided data is invalid.
     */
    T tryMap(CommandContext context, StringReader input) throws CommandException;

    List<String> listSuggestions(CommandContext context, String input);
}
