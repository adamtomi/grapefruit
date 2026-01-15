package grapefruit.command.tree.node;

import grapefruit.command.CommandModule;
import grapefruit.command.util.ToStringer;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static grapefruit.command.util.StringUtil.containsIgnoreCase;
import static java.util.Objects.requireNonNull;

final class InternalCommandNodeImpl<S> implements InternalCommandNode<S> {
    private final String name;
    private final Set<String> aliases;
    private final Set<InternalCommandNode<S>> children;
    private final WeakReference<InternalCommandNode<S>> parent;
    private @Nullable CommandModule<S> command;

    public InternalCommandNodeImpl(final String name, final Set<String> aliases, final @Nullable InternalCommandNode<S> parent) {
        this.name = requireNonNull(name, "name cannot be null");
        // Create a mutable copy of aliases
        this.aliases = new HashSet<>(requireNonNull(aliases, "aliases cannot be null"));
        this.children = new HashSet<>();
        this.parent = new WeakReference<>(parent);
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
    public void mergeAliases(final Set<String> aliases) {
        this.aliases.addAll(aliases);
    }

    @Override
    public boolean matches(final String query) {
        return this.name.equalsIgnoreCase(query) || containsIgnoreCase(query, this.aliases);
    }

    @Override
    public void addChild(final InternalCommandNode<S> child) {
        this.children.add(child);
    }

    @Override
    public void removeChild(final InternalCommandNode<S> child) {
        this.children.remove(child);
    }

    @Override
    public Optional<InternalCommandNode<S>> queryChild(final String query) {
        return this.children.stream().filter(c -> c.matches(query)).findFirst();
    }

    @Override
    public Set<InternalCommandNode<S>> children() {
        return this.children;
    }

    @Override
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    @Override
    public Optional<InternalCommandNode<S>> parent() {
        return Optional.ofNullable(this.parent.get());
    }

    @Override
    public Optional<CommandModule<S>> command() {
        return Optional.ofNullable(this.command);
    }

    @Override
    public void command(final CommandModule<S> command) {
        this.command = requireNonNull(command, "command cannot be null");
    }

    @Override
    public CommandNode asImmutable() {
        return new CommandNodeImpl(this.name, Set.copyOf(this.aliases));
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
        final InternalCommandNodeImpl<?> that = (InternalCommandNodeImpl<?>) o;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.aliases, that.aliases)
                && Objects.equals(this.parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.aliases, this.parent);
    }
}
