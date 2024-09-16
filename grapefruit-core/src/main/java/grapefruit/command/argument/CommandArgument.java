package grapefruit.command.argument;

import grapefruit.command.argument.binding.BoundArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.argument.modifier.ModifierChain;
import grapefruit.command.util.key.Key;

/**
 * Describes an argument of a command. {@link CommandArgument#name()} needs to be
 * unique per argument chain.
 *
 * @param <T> The type of values to be supplied by the user to this argument
 * @see grapefruit.command.argument.mapper.ArgumentMapper
 */
public interface CommandArgument<T> {

    /**
     * The name of the argument. Every argument in
     * an argument list must have a unique name.
     *
     * @return The name of this argument
     */
    String name();

    /**
     * The key of the argument that is used to
     * store and retrieve arguments in and from
     * a {@link grapefruit.command.dispatcher.CommandContext}.
     *
     * @return The key of this argument
     */
    Key<T> key();

    /**
     * The key that's used to retrieve the
     * {@link grapefruit.command.argument.mapper.ArgumentMapper}
     * that is going to map user input passed
     * to this argument.
     *
     * @return The mapper key of this argument
     */
    Key<T> mapperKey();

    /**
     * Returns whether this argument is a flag argument.
     *
     * @return Whether this argument is a flag argument
     * @see FlagArgument
     */
    boolean isFlag();

    /**
     * Casts this command argument to {@link FlagArgument},
     * if possible.
     *
     * @return This cast to {@link FlagArgument}
     * @see FlagArgument
     */
    FlagArgument<T> asFlag();

    /**
     * Returns the {@link ModifierChain} associated with this
     * argument.
     *
     * @return The modifier chain
     * @see ModifierChain
     */
    ModifierChain<T> modifierChain();

    /**
     * Binds this command argument to the supplied {@link ArgumentMapper}
     * instance and returns the resulting {@link BoundArgument}. Binding
     * is performed at the command registration stage and is done
     * to avoid having to perform a lookup for the correct
     * argument mapper every time an argument is being parsed.
     *
     * @param mapper The argument mapper to bind to
     * @return The resulting {@link BoundArgument} instance
     */
    BoundArgument<T> bind(ArgumentMapper<T> mapper);
}
