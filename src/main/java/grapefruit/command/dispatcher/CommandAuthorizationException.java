package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;

import java.io.Serial;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class CommandAuthorizationException extends CommandException {
    @Serial
    private static final long serialVersionUID = 6979248479318168282L;
    private final Set<String> lacking;

    public CommandAuthorizationException(Set<String> lacking) {
        this.lacking = requireNonNull(lacking, "lacking cannot be null");
    }

    public Set<String> lacking() {
        return Set.copyOf(this.lacking);
    }
}
