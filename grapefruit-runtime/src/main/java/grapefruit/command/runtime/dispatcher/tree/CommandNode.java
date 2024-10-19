package grapefruit.command.runtime.dispatcher.tree;

import grapefruit.command.runtime.Command;
import grapefruit.command.runtime.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class CommandNode {
    private static final String ROOT_NAME = "__root__";
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("(\\w|-)+", Pattern.UNICODE_CHARACTER_CLASS);
    private final String primaryAlias;
    private final Set<String> aliases;
    private final Set<CommandNode> children = new LinkedHashSet<>();
    private final CommandNode parent;
    private Command command;

    private CommandNode(String primaryAlias, Set<String> aliases, @Nullable CommandNode parent) {
        validate(primaryAlias);
        aliases.forEach(CommandNode::validate);
        this.primaryAlias = requireNonNull(primaryAlias, "primaryAlias cannot be null");
        // Create a mutable copy
        this.aliases = new HashSet<>(requireNonNull(aliases, "aliases cannot be null"));
        this.parent = parent;
    }

    public static CommandNode from(RouteNode routeNode, @Nullable CommandNode parent) {
        return new CommandNode(routeNode.primaryAlias(), routeNode.secondaryAliases(), parent);
    }

    public static CommandNode root() {
        return new CommandNode(ROOT_NAME, Set.of(), null);
    }

    private static void validate(String input) {
        Matcher matcher = VALID_NAME_PATTERN.matcher(input);
        if (!matcher.matches()) throw new IllegalStateException("String '%s' contains illegal characters".formatted(input));
    }

    public String primaryAlias() {
        return this.primaryAlias;
    }

    public Set<String> aliases() {
        return Set.copyOf(this.aliases);
    }

    public Optional<Command> command() {
        return Optional.ofNullable(this.command);
    }

    public void command(Command command) {
        this.command = requireNonNull(command, "command cannot be null");
    }

    public boolean matches(String alias) {
        return this.primaryAlias.equalsIgnoreCase(alias)
                || StringUtil.containsIgnoreCase(this.aliases, alias);
    }

    public Optional<CommandNode> findChild(String alias) {
        return this.children.stream().filter(x -> x.matches(alias)).findFirst();
    }

    public void addChild(CommandNode child) {
        this.children.add(child);
    }

    public void deleteChild(CommandNode child) {
        this.children.remove(child);
    }

    public Set<CommandNode> children() {
        return Set.copyOf(this.children);
    }

    public Optional<CommandNode> parent() {
        return Optional.ofNullable(this.parent);
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public void mergeAliases(Collection<String> aliases) {
        this.aliases.addAll(aliases);
    }

    @Override
    public String toString() {
        return "CommandNode(primaryAlias=%s, aliases=%s, command=%s)".formatted(
                this.primaryAlias,
                this.aliases,
                this.command
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandNode that = (CommandNode) o;
        return Objects.equals(this.primaryAlias, that.primaryAlias)
                && Objects.equals(this.aliases, that.aliases)
                && Objects.equals(this.command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.primaryAlias, this.aliases, this.command);
    }
}
