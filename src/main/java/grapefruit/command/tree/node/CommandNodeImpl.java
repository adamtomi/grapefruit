package grapefruit.command.tree.node;

import grapefruit.command.util.ToStringer;

import java.util.Set;

import static java.util.Objects.requireNonNull;

final class CommandNodeImpl implements CommandNode {
    private final String name;
    private final Set<String> aliases;

    CommandNodeImpl(final String name, final Set<String> aliases) {
        this.name = requireNonNull(name, "name cannot be null");
        this.aliases = requireNonNull(aliases, "aliases cannot be null");
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Set<String> aliases() {
        return Set.copyOf(this.aliases);
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("name", this.name)
                .append("aliases", this.aliases)
                .toString();
    }
}
