package grapefruit.command.tree.node;

import grapefruit.command.CommandModule;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public interface InternalCommandNode<S> extends CommandNode {

    void mergeAliases(final Set<String> aliases);

    boolean matches(final String query);

    void addChild(final InternalCommandNode<S> child);

    void removeChild(final InternalCommandNode<S> child);

    Optional<InternalCommandNode<S>> queryChild(final String query);

    Set<InternalCommandNode<S>> children();

    boolean isLeaf();

    Optional<InternalCommandNode<S>> parent();

    Optional<CommandModule<S>> command();

    void command(final CommandModule<S> command);

    CommandNode asImmutable();

    static <S> InternalCommandNode<S> of(final String name, final Set<String> aliases, final @Nullable InternalCommandNode<S> parent) {
        return new InternalCommandNodeImpl<>(name, aliases, parent);
    }
}
