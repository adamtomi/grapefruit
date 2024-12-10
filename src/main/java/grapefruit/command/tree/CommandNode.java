package grapefruit.command.tree;

import grapefruit.command.CommandModule;
import grapefruit.command.util.ToStringer;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static grapefruit.command.util.StringUtil.containsIgnoreCase;
import static java.util.Objects.requireNonNull;

public class CommandNode<S> {
    private final String name;
    private final Set<String> aliases;
    private final Set<CommandNode<S>> children;
    private final WeakReference<CommandNode<S>> parent;
    private @Nullable CommandModule<S> command;

    public CommandNode(final String name, final Set<String> aliases, final @Nullable CommandNode<S> parent) {
        this.name = requireNonNull(name, "name cannot be null");
        // Create a mutable copy of aliases
        this.aliases = new HashSet<>(requireNonNull(aliases, "aliases cannot be null"));
        this.children = new HashSet<>();
        this.parent = new WeakReference<>(parent);
    }

    public String name() {
        return this.name;
    }

    public Set<String> aliases() {
        return Set.copyOf(this.aliases);
    }

    public void mergeAliases(final Set<String> aliases) {
        this.aliases.addAll(aliases);
    }

    public boolean matches(final String query) {
        return this.name.equalsIgnoreCase(query) || containsIgnoreCase(query, this.aliases);
    }

    public void addChild(final CommandNode<S> child) {
        this.children.add(child);
    }

    public void removeChild(final CommandNode<S> child) {
        this.children.remove(child);
    }

    public Optional<CommandNode<S>> queryChild(final String query) {
        return this.children.stream().filter(c -> c.matches(query)).findFirst();
    }

    public Set<CommandNode<S>> children() {
        return this.children;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public Optional<CommandNode<S>> parent() {
        return Optional.ofNullable(this.parent.get());
    }

    public Optional<CommandModule<S>> command() {
        return Optional.ofNullable(this.command);
    }

    public void command(final CommandModule<S> command) {
        this.command = requireNonNull(command, "command cannot be null");
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("name", this.name)
                .append("aliases", this.aliases)
                .append("command", this.command)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final CommandNode<?> that = (CommandNode<?>) o;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.aliases, that.aliases)
                && Objects.equals(this.parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.aliases, this.parent);
    }
}
