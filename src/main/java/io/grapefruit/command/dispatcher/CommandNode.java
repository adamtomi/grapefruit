package io.grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CommandNode {
    private final String primary;
    private final Set<String> aliases;
    private final Set<CommandNode> children = new HashSet<>();
    private final @Nullable CommandRegistration registration;

    public CommandNode(final @NotNull String primary,
                       final @NotNull Set<String> aliases,
                       final @Nullable CommandRegistration registration) {
        this.primary = primary;
        this.aliases = aliases;
        this.registration = registration;
    }

    public CommandNode(final @NotNull String primary,
                       final @NotNull String[] aliases,
                       final @Nullable CommandRegistration registration) {
        this(primary, Set.of(aliases), registration);
    }

    public @NotNull String primary() {
        return this.primary;
    }

    public @NotNull Set<String> aliases() {
        return Set.copyOf(this.aliases);
    }

    public void mergeAliases(final @NotNull Iterable<String> aliases) {
        aliases.forEach(this.aliases::add);
    }

    public @NotNull Optional<CommandRegistration> registration() {
        return Optional.ofNullable(this.registration);
    }

    public void addChild(final @NotNull CommandNode child) {
        this.children.add(child);
    }

    public void removeChild(final @NotNull CommandNode child) {
        this.children.remove(child);
    }

    public @NotNull Set<CommandNode> children() {
        return Set.copyOf(this.children);
    }

    public @NotNull Optional<CommandNode> findChild(final @NotNull String name) {
        return this.children.stream()
                .filter(x -> x.primary.equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public String toString() {
        return "CommandNode{" +
                "primary='" + this.primary + '\'' +
                ", aliases=" + this.aliases +
                ", dispatchable=" + this.registration +
                '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CommandNode that = (CommandNode) o;
        return Objects.equals(this.primary, that.primary)
                && Objects.equals(this.aliases, that.aliases)
                && Objects.equals(this.registration, that.registration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.primary, this.aliases, this.registration);
    }
}
