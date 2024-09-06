package grapefruit.command.argument.chain;

import grapefruit.command.CommandExecutable;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.util.ValueFactory;

/**
 * Represents a command argument bound to a value factory.
 *
 * @param <T> The argument type
 * @param <C> The {@link CommandArgument} instance type
 */
public interface BoundArgument<T, C extends CommandArgument<T>> extends CommandExecutable {

    /**
     * @return The contained argument
     */
    C argument();

    /**
     * @return The value factory that {@link this#argument()}
     * is bound to
     */
    ValueFactory<T> valueFactory();

    /**
     * Constructs a new binding for positional
     * (required) arguments.
     *
     * @param <T> The argument type
     * @param argument The argument itself
     * @param factory The value factory
     * @return The constructed argument
     */
    static <T> Positional<T> arg(CommandArgument<T> argument, ValueFactory<T> factory) {
        return new Impl.PositionalBinding<>(argument, factory);
    }

    /**
     * Constructs a new binding for flag
     * (optional) arguments.
     *
     * @param <T> The argument type
     * @param argument The argument itself
     * @param factory The value factory
     * @return The constructed argument
     */
    static <T> Flag<T> flag(FlagArgument<T> argument, ValueFactory<T> factory) {
        return new Impl.FlagBinding<>(argument, factory);
    }

    interface Flag<T> extends BoundArgument<T, FlagArgument<T>> {}

    interface Positional<T> extends BoundArgument<T, CommandArgument<T>> {}
}
