package grapefruit.command.dispatcher.syntax;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.input.StringReader;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

/**
 * This exception is thrown when command input-related issues
 * are detected, such as too few or too many arguments.
 */
public class CommandSyntaxException extends CommandException {
    @Serial
    private static final long serialVersionUID = -6615337831318406658L;
    private final CommandSyntax correctSyntax;
    private final String originalInput;
    private final String consumedInput;
    private final Reason reason;

    private CommandSyntaxException(
            CommandSyntax correctSyntax,
            String originalInput,
            String consumedInput,
            Reason reason
    ) {
        this.correctSyntax = requireNonNull(correctSyntax, "correctSyntax");
        this.originalInput = requireNonNull(originalInput, "originalInput");
        this.consumedInput = requireNonNull(consumedInput, "consumedInput");
        this.reason = requireNonNull(reason, "kind");
    }

    /**
     * Generates a new {@link CommandSyntaxException} from the supplied reader.
     *
     * @param reader The reader to generate syntax from
     */
    public static CommandSyntaxException from(StringReader reader, Command command, Reason reason) {
        return new CommandSyntaxException(
                CommandSyntax.create(command),
                reader.unwrap(),
                reader.consumed(),
                reason
        );
    }

    /**
     * Retuns the pre-generated correct syntax.
     *
     * @return The correct syntax
     */
    public CommandSyntax correctSyntax() {
        return this.correctSyntax;
    }

    /**
     * Returns the original input supplied by the user.
     *
     * @return The original command input
     */
    public String originalInput() {
        return this.originalInput;
    }

    /**
     * Returns the consumed part of the original command
     * input.
     *
     * @return The consumed part of the input.
     */
    public String consumedInput() {
        return this.consumedInput;
    }

    /**
     * Returns the reason of this exception.
     *
     * @return The reason
     */
    public Reason reason() {
        return this.reason;
    }

    public enum Reason {
        /**
         * Set if there are no more consumable arguments left in the
         * {@link StringReader}, but more is expected either by
         * and {@link grapefruit.command.argument.mapper.ArgumentMapper}
         * or the {@link grapefruit.command.dispatcher.tree.CommandGraph}.
         */
        TOO_FEW_ARGUMENTS,
        /**
         * Set if the current {@link StringReader} still has arguments
         * to read, but nothing requests more.
         */
        TOO_MANY_ARGUMENTS
    }
}
