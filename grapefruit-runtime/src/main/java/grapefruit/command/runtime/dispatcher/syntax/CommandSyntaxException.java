package grapefruit.command.runtime.dispatcher.syntax;

import grapefruit.command.runtime.CommandException;
import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.dispatcher.CommandDefinition;
import grapefruit.command.runtime.dispatcher.input.StringReader;
import grapefruit.command.runtime.dispatcher.tree.CommandGraph;
import grapefruit.command.runtime.generated.CommandMirror;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * This exception is thrown when command input-related issues
 * are detected, such as too few or too many arguments.
 */
public class CommandSyntaxException extends CommandException {
    @Serial
    private static final long serialVersionUID = -6615337831318406658L;
    private final @Nullable CommandSyntax correctSyntax;
    private final String originalInput;
    private final String consumedInput;
    private final Reason reason;

    private CommandSyntaxException(
            @Nullable CommandSyntax correctSyntax,
            String originalInput,
            String consumedInput,
            Reason reason
    ) {
        this.correctSyntax = correctSyntax;
        this.originalInput = requireNonNull(originalInput, "originalInput");
        this.consumedInput = requireNonNull(consumedInput, "consumedInput");
        this.reason = requireNonNull(reason, "kind");
    }

    /**
     * Generates a new {@link CommandSyntaxException} from the supplied reader.
     *
     * @param reader The reader to generate syntax from
     */
    public static CommandSyntaxException from(StringReader reader, @Nullable CommandMirror mirror, @Nullable CommandDefinition command, Reason reason) {
        return new CommandSyntaxException(
                command != null ? CommandSyntax.create(mirror, command) : null,
                reader.unwrap(),
                reader.consumed(),
                reason
        );
    }

    /**
     * Retuns the pre-generated correct syntax, if it exists.
     *
     * @return The correct syntax
     */
    public Optional<CommandSyntax> correctSyntax() {
        return Optional.ofNullable(this.correctSyntax);
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
         * and {@link ArgumentMapper}
         * or the {@link CommandGraph}.
         */
        TOO_FEW_ARGUMENTS,
        /**
         * Set if the current {@link StringReader} still has arguments
         * to read, but nothing requests more.
         */
        TOO_MANY_ARGUMENTS
    }
}
