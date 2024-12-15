package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandChain;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class CommandSyntaxException extends CommandException {
    @Serial
    private static final long serialVersionUID = -2348379300575195708L;
    private final CommandChain<?> chain;
    private final Reason reason;

    public CommandSyntaxException(final CommandChain<?> chain, final Reason reason) {
        this.chain = requireNonNull(chain, "chain cannot be null");
        this.reason = requireNonNull(reason, "reason cannot be null");
    }

    public CommandChain<?> chain() {
        return this.chain;
    }

    public Reason reason() {
        return this.reason;
    }

    public enum Reason {
        TOO_MANY_ARGUMENTS,
        TOO_FEW_ARGUMENTS
    }
}
