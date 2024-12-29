package grapefruit.command.tree;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.tree.node.CommandNode;

import java.io.Serial;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class NoSuchCommandException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = 8554442705689440988L;
    private final Set<CommandNode> alternatives;

    public NoSuchCommandException(final String consumed, final String argument, final String remaining, final Set<CommandNode> alternatives) {
        super(consumed, argument, remaining);
        this.alternatives = requireNonNull(alternatives, "alternatives cannot be null");
    }

    @Deprecated
    public static NoSuchCommandException fromInput(final CommandInputTokenizer input, final String argument, final Set<CommandNode> validAlternatives) {
        return new NoSuchCommandException(input.consumed(), argument, input.remainingOrEmpty(), validAlternatives);
    }

    public Set<CommandNode> alternatives() {
        return Set.copyOf(this.alternatives);
    }
}
