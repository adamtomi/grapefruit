package grapefruit.command.tree;

import grapefruit.command.CommandException;
import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CommandGraph<S> {
    private final CommandNode<S> rootNode = new CommandNode<>("__ROOT__", Set.of(), null);

    public void insert(final CommandChain<S> chain, final CommandModule<S> command) {
        requireNonNull(chain, "chain cannot be null");
        requireNonNull(command, "command cannot be null");

        if (chain.route().isEmpty()) {
            throw new IllegalStateException("Cannot register a command handler directly on the root node");
        }

        CommandNode<S> node = this.rootNode;
        for (final Iterator<CommandArgument.Literal> iter = chain.route().iterator(); iter.hasNext();) {
            final CommandArgument.Literal literal = iter.next();
            boolean isLast = !iter.hasNext();

            final Optional<CommandNode<S>> childCandidate = queryChildOf(node, literal);
            if (childCandidate.isPresent()) {
                final CommandNode<S> child = childCandidate.orElseThrow();
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
                final CommandNode<S> child = new CommandNode<>(literal.name(), literal.aliases(), node);
                node.addChild(child);
                node = child;
                // Register command if we're at the end of the chain
                if (isLast) node.command(command);
            }
        }
    }

    public void delete(final CommandChain<S> chain) {
        requireNonNull(chain, "chain cannot be null");
        if (chain.route().isEmpty() || this.rootNode.isLeaf()) return;

        CommandNode<S> node = this.rootNode;
        for (final CommandArgument.Literal literal : chain.route()) {
            final Optional<CommandNode<S>> childCandidate = queryChildOf(node, literal);
            if (childCandidate.isEmpty()) {
                throw new IllegalStateException("Command node '%s' does not have a suitable child".formatted(node));
            }

            node = childCandidate.orElseThrow();
        }

        // Check just in case
        if (!node.isLeaf()) throw new IllegalStateException("Attempting to delete non-leaf command node");
        while (!node.equals(this.rootNode)) {
            final CommandNode<S> parent = node.parent().orElseThrow();
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
        CommandNode<S> node = this.rootNode;
        while (true) {
            final String name = input.readWord();
            final Optional<CommandNode<S>> childCandidate = node.queryChild(name);
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
    }

    private static <S> Optional<CommandNode<S>> queryChildOf(final CommandNode<S> parent, final CommandArgument.Literal literal) {
        Optional<CommandNode<S>> candidate = parent.queryChild(literal.name());
        if (candidate.isPresent()) return candidate;

        for (String alias : literal.aliases()) {
            candidate = parent.queryChild(alias);
            if (candidate.isPresent()) return candidate;
        }

        return Optional.empty();
    }

    private static <S> NoSuchCommandException generateNoSuchCommand(final CommandNode<S> node, final CommandInputTokenizer input, final String argument) {
        final Set<NoSuchCommandException.Alternative> alternatives = node.children().stream()
                .map(x -> new NoSuchCommandException.Alternative(x.name(), x.aliases()))
                .collect(Collectors.toSet());

        return NoSuchCommandException.fromInput(input, argument, alternatives);
    }
}
