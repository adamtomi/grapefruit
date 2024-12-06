package grapefruit.command.dispatcher.input;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandChain;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class CommandSyntaxException extends CommandException {
    @Serial
    private static final long serialVersionUID = -2348379300575195708L;
    private final CommandChain<?> chain;
    private final Reason reason;

    public CommandSyntaxException(final @Nullable CommandChain<?> chain, final Reason reason) {
        this.chain = chain;
        this.reason = requireNonNull(reason, "reason cannot be null");
    }

    public Optional<CommandChain<?>> chain() {
        return Optional.ofNullable(this.chain);
    }

    public Reason reason() {
        return this.reason;
    }

    public enum Reason {
        TOO_MANY_ARGUMENTS,
        TOO_FEW_ARGUMENTS
    }
}
