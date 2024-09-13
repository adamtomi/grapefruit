package grapefruit.command.argument.modifier;

import grapefruit.command.argument.CommandArgumentException;

/**
 * Argument modifiers can be used to:
 * <ul>
 *     <li>Verify, that the input meets certain criteria</li>
 *     <li>Can alter the input (but cannot change the its type)</li>
 * </ul>
 * They receive input mapped by an {@link grapefruit.command.argument.mapper.ArgumentMapper}
 * which they can then operate on.
 *
 * @param <T> The expected data type
 */
public interface ArgumentModifier<T> {

    /**
     * Operates on the supplied input and returns the result,
     * or throws an exception, if the data is deemed invalid.
     *
     * @param input The input
     * @return The modified argument
     * @throws CommandArgumentException If the argument does not
     * meet certain criteria
     */
    T apply(T input) throws CommandArgumentException;
}
