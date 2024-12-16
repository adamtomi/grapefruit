package grapefruit.command.tree;

import grapefruit.command.CommandException;
import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.input.MissingInputException;
import grapefruit.command.tree.node.CommandNode;
import grapefruit.command.tree.node.InternalCommandNode;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CommandGraph<S> {
    private final InternalCommandNode<S> rootNode = InternalCommandNode.of("__ROOT__", Set.of(), null);

    public void insert(final CommandChain<S> chain, final CommandModule<S> command) {
        requireNonNull(chain, "chain cannot be null");
        requireNonNull(command, "command cannot be null");

        if (chain.route().isEmpty()) {
            throw new IllegalStateException("Cannot register a command handler directly on the root node");
        }

        InternalCommandNode<S> node = this.rootNode;
        for (final Iterator<CommandArgument.Literal<S>> iter = chain.route().iterator(); iter.hasNext();) {
            final CommandArgument.Literal<S> literal = iter.next();
            boolean isLast = !iter.hasNext();

            final Optional<InternalCommandNode<S>> childCandidate = queryChildOf(node, literal);
            if (childCandidate.isPresent()) {
                final InternalCommandNode<S> child = childCandidate.orElseThrow();
                if (isLast) {
                    /*
                     * We throw an exception if a child node exists, and we don't have
                     * any route parts left, because either this child has child nodes
                     * or already has a command handler attached to it (otherwise the
                     * chain would be invalid). In any case, we cannot proceed from here.
                     */
                    throw new IllegalStateException("Command node '%s' already exists in the command graph".formatted(child));
                }

                child.mergeAliases(literal.aliases());
                node = child;
            } else {
                final InternalCommandNode<S> child = InternalCommandNode.of(literal.name(), literal.aliases(), node);
                node.addChild(child);
                node = child;
                // Register command if we're at the end of the chain
                if (isLast) node.command(command);
            }
        }
    }

    public void delete(final CommandChain<S> chain) {
        requireNonNull(chain, "chain cannot be null");
        if (chain.route().isEmpty()) return;
        if (this.rootNode.isLeaf()) throw new IllegalStateException("Root node is leaf");

        InternalCommandNode<S> node = this.rootNode;
        for (final CommandArgument.Literal<S> literal : chain.route()) {
            final Optional<InternalCommandNode<S>> childCandidate = queryChildOf(node, literal);
            if (childCandidate.isEmpty()) {
                throw new IllegalStateException("Command node '%s' does not have a suitable child".formatted(node));
            }

            node = childCandidate.orElseThrow();
        }

        // Check just in case
        if (!node.isLeaf()) throw new IllegalStateException("Attempting to delete non-leaf command node");
        while (!node.equals(this.rootNode)) {
            final InternalCommandNode<S> parent = node.parent().orElseThrow();
            // If the node is a leaf node, we can safely delete it from its parent
            if (node.isLeaf()) {
                parent.removeChild(node);
                node = parent;
            } else {
                // If the node is not a leaf node, we can't delete more nodes.
                break;
            }
        }
    }

    public CommandModule<S> search(final CommandInputTokenizer input) throws CommandException {
        InternalCommandNode<S> node = this.rootNode;
        try {
            while (true) {
                final String name = input.readWord();
                final Optional<InternalCommandNode<S>> childCandidate = node.queryChild(name);
                if (childCandidate.isEmpty()) {
                    throw generateNoSuchCommand(node, input, name);
                }

                node = childCandidate.orElseThrow();
                if (node.isLeaf()) {
                    final Optional<CommandModule<S>> commandCandidate = node.command();
                    if (commandCandidate.isPresent()) return commandCandidate.orElseThrow();

                    /*
                     * If the node is a leaf node, we assume this is the command handler
                     * we're looking for. Technically it should always exist, because
                     * leaf nodes must have a command attached to them, and this#insert
                     * makes sure of that. Just to be safe though, if the command handler
                     * still happens to be missing.
                     */
                    throw new IllegalStateException("CommandNode '%s' is a leaf node, but has no command attached to it.".formatted(node));
                }
            }
        } catch (final MissingInputException ex) {
            throw generateNoSuchCommand(node, input, "");
        }
    }

    private static <S> Optional<InternalCommandNode<S>> queryChildOf(final InternalCommandNode<S> parent, final CommandArgument.Literal<S> literal) {
        Optional<InternalCommandNode<S>> candidate = parent.queryChild(literal.name());
        if (candidate.isPresent()) return candidate;

        for (String alias : literal.aliases()) {
            candidate = parent.queryChild(alias);
            if (candidate.isPresent()) return candidate;
        }

        return Optional.empty();
    }

    private static <S> NoSuchCommandException generateNoSuchCommand(final InternalCommandNode<S> node, final CommandInputTokenizer input, final String argument) {
        final Set<CommandNode> alternatives = node.children().stream()
                .map(InternalCommandNode::asImmutable)
                .collect(Collectors.toSet());

        return NoSuchCommandException.fromInput(input, argument, alternatives);
    }
}
