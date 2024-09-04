package grapefruit.command.argument;

import grapefruit.command.argument.modifier.ArgumentModifier;
import grapefruit.command.util.key.Key;

import java.util.Set;

/**
 * Describes an argument of a command.
 *
 * @param <T> The type of argument.
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
    // TODO remove default, every argument needs to implement this.
    default Key<T> mapperKey() {
        return key();
    }

    /**
     * Returns whether this argument is a flag argument.
     *
     * @return Whether this argument is a flag argument
     * @see FlagArgument
     */
    boolean isFlag();

    /**
     * Returns a set of modifiers associated with this
     * argument.
     *
     * @return Modifiers associated with this argument
     * @see ArgumentModifier
     */
    default Set<ArgumentModifier<T>> modifiers() {
        return Set.of();
    }
}
