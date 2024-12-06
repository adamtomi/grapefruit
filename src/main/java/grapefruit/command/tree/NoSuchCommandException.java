package grapefruit.command.tree;

import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;

import java.io.Serial;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class NoSuchCommandException extends CommandArgumentException {
    @Serial
    private static final long serialVersionUID = 8554442705689440988L;
    private final Set<Alternative> alternatives;

    private NoSuchCommandException(final String consumed, final String argument, final String remaining, final Set<Alternative> alternatives) {
        super(consumed, argument, remaining);
        this.alternatives = requireNonNull(alternatives, "alternatives cannot be null");
    }

    public static NoSuchCommandException fromInput(final CommandInputTokenizer input, final String argument, final Set<Alternative> validAlternatives) {
        return new NoSuchCommandException(input.consumed(), argument, input.remainingOrEmpty(), validAlternatives);
    }

    public Set<Alternative> alternatives() {
        return Set.copyOf(this.alternatives);
    }

    public record Alternative(String name, Set<String> aliases) {
        public Alternative {
            requireNonNull(name, "name cannot be null");
            requireNonNull(aliases, "aliases cannot be null");
        }
    }
}
