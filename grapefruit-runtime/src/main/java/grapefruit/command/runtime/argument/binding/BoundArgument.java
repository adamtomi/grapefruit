package grapefruit.command.runtime.argument.binding;

import grapefruit.command.runtime.CommandException;
import grapefruit.command.runtime.argument.CommandArgument;
import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.dispatcher.CommandContext;
import grapefruit.command.runtime.dispatcher.input.StringReader;

/**
 * Represents a command argument bound to a value factory.
 *
 * @param <T> The argument type
 */
@Deprecated
public interface BoundArgument<T> {

    /**
     * @return The contained argument
     */
    CommandArgument<T> argument();

    /**
     * @return The value factory that {@link this#argument()}
     * is bound to
     */
    ArgumentMapper<T> mapper();

    /**
     * Attempts to consume one (or multiple) argument(s) from
     * the provided input.
     *
     * @param context The current context
     * @param input The user input
     * @throws CommandException If there are no more arguments
     * left to consume, or there's an error processing user input
     */
    void consume(CommandContext context, StringReader input) throws CommandException;

    /**
     * Creates and returns a new {@link BoundArgument} instance containing
     * the supplied the argument and mapper.
     *
     * @param <T> The type of the argument
     * @param argument The argument to bind
     * @param mapper The mapper to bind to
     * @return The created bound argument
     */
    static <T> BoundArgument<T> of(CommandArgument<T> argument, ArgumentMapper<T> mapper) {
        return new BoundArgumentImpl<>(argument, mapper);
    }
}
