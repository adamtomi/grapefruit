package grapefruit.command.argument.chain;

import java.util.List;

/**
 * Stores command argument information for a specific command.
 * Each argument is stored as a {@link BoundArgument} instance;
 * flags and positional arguments have their own separate lists
 * to simplify command parsing and generating suggestions.
 */
public interface ArgumentChain {

    /**
     * @return The list of positional bound arguments.
     */
    List<BoundArgument.Positional<?>> positional();

    /**
     * @return The list of flag bound arguments.
     */
    List<BoundArgument.Flag<?>> flag();

    /**
     * Creates a new chain with the supplied arguments.
     *
     * @param positional Positional arguments
     * @param flag Flag arguments
     * @return The created chain
     */
    static ArgumentChain create(
            List<BoundArgument.Positional<?>> positional,
            List<BoundArgument.Flag<?>> flag
    ) {
        return new Impl.ArgumentChainImpl(positional, flag);
    }
}
