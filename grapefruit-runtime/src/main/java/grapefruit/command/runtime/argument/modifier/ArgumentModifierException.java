package grapefruit.command.runtime.argument.modifier;

import grapefruit.command.runtime.argument.CommandArgumentException;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

/**
 * Thrown by {@link ArgumentModifier modifiers} if user input cannot be
 * modified the modifier (for instance it doesn't meet certain requirements).
 */
public class ArgumentModifierException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = -5512071934757415510L;
    private final ArgumentModifier<?> modifier;

    public ArgumentModifierException(String input, ArgumentModifier<?> modifier) {
        super(input);
        this.modifier = requireNonNull(modifier, "modifier cannot be null");
    }

    /**
     * @return The causing modifier
     */
    public ArgumentModifier<?> modifier() {
        return this.modifier;
    }
}
