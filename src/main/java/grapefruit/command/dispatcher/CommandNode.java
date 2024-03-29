package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class CommandNode<S> {
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("(\\w|-)+", Pattern.UNICODE_CHARACTER_CLASS);
    private final String primary;
    private final Set<String> aliases;
    private final Set<CommandNode<S>> children = new LinkedHashSet<>();
    private @Nullable CommandRegistration<S> registration;

    public CommandNode(final @NotNull String primary,
                       final @NotNull Set<String> aliases,
                       final @Nullable CommandRegistration<S> registration) {
        validate(primary);
        aliases.forEach(CommandNode::validate);
        this.primary = primary;
        // Create a mutable copy, so #mergeAliases doesn't fail
        this.aliases = new LinkedHashSet<>(aliases);
        this.registration = registration;
    }

    public CommandNode(final @NotNull String primary,
                       final @NotNull String[] aliases,
                       final @Nullable CommandRegistration<S> registration) {
        this(primary, Miscellaneous.mutableCollectionOf(aliases, LinkedHashSet::new), registration);
    }

    private static void validate(final @NotNull String name) {
        final Matcher matcher = VALID_NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(format("Name '%s' does not match '%s'", name, VALID_NAME_PATTERN));
        }
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
