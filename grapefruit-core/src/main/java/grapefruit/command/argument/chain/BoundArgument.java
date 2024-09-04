package grapefruit.command.argument.chain;

import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;

/**
 * Represents a command argument bound to an argument mapper.
 *
 * @param <T> The argument type
 * @param <C> The {@link CommandArgument} instance type
 */
public interface BoundArgument<T, C extends CommandArgument<T>> {

    /**
     * @return The contained argument
     */
    C argument();

    /**
     * @return The mapper {@link this#argument()} is bound to.
     */
    ArgumentMapper<T> mapper();

    /**
     * Constructs a new binding for positional
     * (required) arguments.
     *
     * @param <T> The argument type
     * @param argument The argument itself
     * @param mapper The mapper to bind to
     * @return The constructed argument
     */
    static <T> Positional<T> arg(CommandArgument<T> argument, ArgumentMapper<T> mapper) {
        return new Impl.PositionalBinding<>(argument, mapper);
    }

    /**
     * Constructs a new binding for flag
     * (optional) arguments.
     *
     * @param <T> The argument type
     * @param argument The argument itself
     * @param mapper The mapper to bind to
     * @return The constructed argument
     */
    static <T> Flag<T> flag(FlagArgument<T> argument, ArgumentMapper<T> mapper) {
        return new Impl.FlagBinding<>(argument, mapper);
    }

    interface Flag<T> extends BoundArgument<T, FlagArgument<T>> {}

    interface Positional<T> extends BoundArgument<T, CommandArgument<T>> {}
}
