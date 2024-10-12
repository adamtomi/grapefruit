package grapefruit.command.dispatcher.syntax;

import grapefruit.command.CommandException;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

// TODO see if it's possible to merge this with CommandSyntaxException somehow
/**
 * This exception is thrown if the same flag occurs more than once
 * in the command input.
 */
public class DuplicateFlagException extends CommandException {
    @Serial
    private static final long serialVersionUID = -8812418817991716419L;
    private final String flagName;

    public DuplicateFlagException(String flagName) {
        this.flagName = requireNonNull(flagName, "flagName cannot be null");
    }

    /**
     * Returns the flag identifier that has been set
     * more than once.
     *
     * @return The flag name
     */
    public String flagIdentifier() {
        return this.flagName;
    }
}
