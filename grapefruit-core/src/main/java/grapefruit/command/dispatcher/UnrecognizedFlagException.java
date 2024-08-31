package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

/**
 * This exception is thrown if the command input contains
 * flag names that aren't valid parameters of the requested
 * command.
 */
public class UnrecognizedFlagException extends CommandException {
    @Serial
    private static final long serialVersionUID = -913906498978955944L;
    private final String name;

    public UnrecognizedFlagException(String name) {
        this.name = requireNonNull(name, "name cannot be null");
    }

    public UnrecognizedFlagException(char shorthand) {
        this(Character.toString(shorthand));
    }

    /**
     * Returns the name that was passed as a flag name.
     *
     * @return The unrecognized flag name
     */
    public String name() {
        return this.name;
    }
}
