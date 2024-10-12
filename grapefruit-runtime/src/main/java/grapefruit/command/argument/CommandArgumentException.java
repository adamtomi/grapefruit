package grapefruit.command.argument;

import grapefruit.command.CommandException;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

/**
 * This exception is thrown when some part of the user input is deemed invalid
 * for one of the following reasons:
 * <ul>
 *     <li>
 *         An {@link grapefruit.command.argument.modifier.ArgumentModifier} fails to map the input into another type.
 *     </li>
 *     <li>
 *         An {@link grapefruit.command.argument.modifier.ArgumentModifier} fails to modify the input.
 *     </li>
 * </ul>
 */
public class CommandArgumentException extends CommandException {
    @Serial
    private static final long serialVersionUID = -8437065396865235634L;
    private final String input;

    public CommandArgumentException(String input) {
        this.input = requireNonNull(input, "input cannot be null");
    }

    /**
     * Returns the part of the user input that was deemed invalid.
     *
     * @return The invalid input
     */
    public String input() {
        return this.input;
    }
}
