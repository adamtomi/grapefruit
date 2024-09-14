package grapefruit.command.dispatcher.tree;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.input.StringReader;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class CommandGraph {
    /* The internal command tree */
    private final CommandNode rootNode = CommandNode.root();

    /**
     * Inserts the given command into the command tree.
     *
     * @param command The command to insert
     */
    public void insert(Command command) {
        requireNonNull(command, "command cannot be null");
        List<RouteNode> parts = command.spec().route();
        /*
         * A command handler cannot be registered directly on the
         * root node, at least one route part is required.
         */
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Cannot register a command handler on the root node");
        }

        CommandNode node = this.rootNode;
        // Loop through the route parts
        for (Iterator<RouteNode> iter = parts.iterator(); iter.hasNext();) {
            RouteNode part = iter.next();
            boolean isLast = !iter.hasNext();

            // Attempt to find an existing child node with the
            // provided aliases
            Optional<CommandNode> childCandidate = findChild(node, part);
            if (childCandidate.isEmpty()) {
                // Create new child node
                CommandNode temp = CommandNode.from(part, node);
                node.addChild(temp);
                node = temp;
                // Register command if we're at the end of the chain
                if (isLast) node.command(command);
            } else {
                CommandNode childNode = childCandidate.orElseThrow();
                if (isLast) {
                    /*
                     * We throw an exception if a child node exists, and we don't have
                     * any route parts left, because either this child has child nodes
                     * or already has a command handler attached to it (otherwise the
                     * chain would be invalid). In any case, we cannot proceed from here.
                     */
                    throw new IllegalArgumentException("Node '%s' already exists in the command chain".formatted(childNode));
                }

                childNode.mergeAliases(part.secondaryAliases());
                node = childNode;
            }
        }
    }

    /**
     * Deletes the given command from the command tree.
     *
     * @param command The command to delete
     */
    public void delete(Command command) {
        requireNonNull(command, "command cannot be null");
        List<RouteNode> parts = command.spec().route();
        // There's nothing to delete if the list is empty
        if (parts.isEmpty()) return;

        CommandNode node = this.rootNode;
        // Find the last node in this command chain
        for (RouteNode part : parts) {
            Optional<CommandNode> childCandidate = findChild(node, part);
            if (childCandidate.isEmpty()) {
                // This shouldn't happen
                throw new IllegalStateException("Command node '%s' does not have a suitable child.".formatted(node));
            }

            node = childCandidate.orElseThrow();
        }

        // Check just in case
        if (!node.isLeaf()) throw new IllegalStateException("Attempting to delete a non-leaf command node.");
        while (!node.equals(this.rootNode)) {
            CommandNode parent = node.parent().orElseThrow();
            // If the node is a leaf node, we can safely delete it from its parent
            if (node.isLeaf()) {
                parent.deleteChild(node);
                node = parent;
            } else {
                // If the node is not a leaf node, we can't delete more nodes.
                break;
            }
        }
    }

    /**
     * Attempts to find a {@link Command} instance attached to a
     * {@link CommandNode} based on user input.
     *
     * @param input The reader wrapping user input
     * @return The command if it was found
     */
    public SearchResult search(StringReader input) {
        CommandNode node = this.rootNode;
        try {
            while (true) {
                String name = input.readSingle();
                // Attempt to find a child node with this name
                Optional<CommandNode> childCandidate = node.findChild(name);
                if (childCandidate.isEmpty()) {
                    // There is no child node with the provided name, we throw an exception
                    throw new NoSuchCommandException(name, input.consumed());
                }

                node = childCandidate.orElseThrow();
                if (node.isLeaf()) {
                    Optional<Command> command = node.command();
                    // Not using Optional#orElseThrow(String), because node isn't final
                    if (command.isPresent()) return SearchResult.success(command.orElseThrow());

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
        } catch (CommandException ex) {
            return SearchResult.failure(node, ex);
        }
    }

    /**
     * Finds a child node of the supplied parent node by primary alias
     * or secondary aliases.
     *
     * @param parent The parent node
     * @param node The route node containing the aliases
     */
    private Optional<CommandNode> findChild(CommandNode parent, RouteNode node) {
        Optional<CommandNode> found = parent.findChild(node.primaryAlias());
        if (found.isEmpty()) {
            for (String alias : node.secondaryAliases()) found = parent.findChild(alias);
        }

        return found;
    }

    /**
     * This exception indicated that no command node with the provided name
     * could be found.
     */
    public static class NoSuchCommandException extends CommandException {
        @Serial
        private static final long serialVersionUID = 1581132770833148304L;
        private final String name;
        private final String consumedArgs;

        private NoSuchCommandException(String name, String consumedArgs) {
            super();
            this.name = requireNonNull(name, "name cannot be null");
            this.consumedArgs = requireNonNull(consumedArgs, "consumedArgs cannot be null");
        }

        /**
         * Returns the supplied name.
         *
         * @return The supplied name
         */
        public String name() {
            return this.name;
        }

        /**
         * Returns the part of the user input that has successfully
         * been consumed.
         *
         * @return The consumed part of the user input
         */
        public String consumedArgs() {
            return this.consumedArgs;
        }
    }

    /**
     * Represents a command search result. Implementations provide
     * more information.
     */
    public interface SearchResult {

        /**
         * Constructs a successful search result.
         *
         * @param command The command instance
         * @return The created search result
         */
        static SearchResult success(Command command) {
            return new Success(command);
        }

        /**
         * Constructs a failed search result.
         *
         * @param lastMatchedNode The last matched command node
         * @param cause The cause of this failure
         * @return The created search result
         */
        static SearchResult failure(CommandNode lastMatchedNode, CommandException cause) {
            return new Failure(lastMatchedNode, cause);
        }

        /**
         * Successful search result implementation.
         */
        record Success(Command command) implements SearchResult {
            public Success {
                requireNonNull(command, "command cannot be null");
            }
        }

        /**
         * Failed search result implementation.
         */
        record Failure(CommandNode lastMatchedNode, CommandException cause) implements SearchResult {
            public Failure {
                requireNonNull(lastMatchedNode, "lastMatchedNode cannot be null");
                requireNonNull(cause, "cause cannot be null");
            }

            /**
             * Creates a list of valid options, which in this context
             * means the primary (and optionally secondary) aliases
             * of all child nodes belonging to {@link this#lastMatchedNode}.
             *
             * @param includeSecondary Whether to include secondary aliases too
             * @return The constructed list
             */
            public List<String> validOptions(boolean includeSecondary) {
                if (includeSecondary) {
                    List<String> options = new ArrayList<>();
                    for (CommandNode child : lastMatchedNode().children()) {
                        options.add(child.primaryAlias());
                        options.addAll(child.aliases());
                    }

                    // Create an immutable copy so that we always return an immutable list in this method
                    return List.copyOf(options);
                }

                return lastMatchedNode().children().stream()
                        .map(CommandNode::primaryAlias)
                        .toList();
            }
        }
    }
}
