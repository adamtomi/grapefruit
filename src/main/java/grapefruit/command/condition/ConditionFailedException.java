package grapefruit.command.condition;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

import static java.util.Objects.requireNonNull;

public class ConditionFailedException extends CommandException {
    @Serial
    private static final long serialVersionUID = 5981670248217650172L;
    private final String id;
    private final CommandContext<?> context;

    public ConditionFailedException(final @NotNull String id, final @NotNull CommandContext<?> context) {
        // TODO
        super(null, null);
        this.id = requireNonNull(id, "id cannot be null");
        this.context = requireNonNull(context, "context cannot be null");
    }

    public @NotNull String id() {
        return this.id;
    }

    public @NotNull CommandContext<?> context() {
        return this.context;
    }
}
