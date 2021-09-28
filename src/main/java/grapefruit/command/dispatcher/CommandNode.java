package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CommandNode<S> {
    private final String primary;
    private final Set<String> aliases;
    private final Set<CommandNode<S>> children = new HashSet<>();
    private @Nullable CommandRegistration<S> registration;

    public CommandNode(final @NotNull String primary,
                       final @NotNull Set<String> aliases,
                       final @Nullable CommandRegistration<S> registration) {
        this.primary = primary;
        this.aliases = aliases;
        this.registration = registration;
    }

    public CommandNode(final @NotNull String primary,
                       final @NotNull String[] aliases,
                       final @Nullable CommandRegistration<S> registration) {
        this(primary, Miscellaneous.mutableCollectionOf(aliases, HashSet::new), registration);
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

    public @NotNull Optional<CommandRegistration<S>> registration() {
        return Optional.ofNullable(this.registration);
    }

    public void registration(final @NotNull CommandRegistration<S> registration) {
        this.registration = registration;
    }

    public void addChild(final @NotNull CommandNode<S> child) {
        this.children.add(child);
    }

    public void removeChild(final @NotNull CommandNode<S> child) {
        this.children.remove(child);
    }

    public @NotNull Set<CommandNode<S>> children() {
        return Set.copyOf(this.children);
    }

    public @NotNull Optional<CommandNode<S>> findChild(final @NotNull String name) {
        return this.children.stream()
                .filter(x -> x.primary.equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public String toString() {
        return "CommandNode[" +
                "primary='" + this.primary + '\'' +
                ", aliases=" + this.aliases +
                ", registration=" + this.registration +
                ", children=" + this.children +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CommandNode<?> that = (CommandNode<?>) o;
        return Objects.equals(this.primary, that.primary)
                && Objects.equals(this.aliases, that.aliases)
                && Objects.equals(this.registration, that.registration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.primary, this.aliases, this.registration);
    }
}
