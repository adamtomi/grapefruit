package grapefruit.command.dispatcher.tree;

import grapefruit.command.Command;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CommandGraph {
    /* The internal command tree */
    private final CommandNode rootNode = new CommandNode("__ROOT__", Set.of(), null, null);

    /**
     * Inserts the given command into the command tree.
     *
     * @param command The command to insert
     */
    public void insert(Command command) {
        requireNonNull(command, "command cannot be null");
        /*
         * Split the command route into parts. Each individual part
         * will become a command node in the tree if the registration
         * succeeds.
         */
        List<RoutePart> parts = Arrays.stream(command.meta().route().split(" "))
                .map(RoutePart::of)
                .toList();
        /*
         * A command handler cannot be registered directly on the
         * root node, at least one route part is required.
         */
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Cannot register a command handler on the root node");
        }

        CommandNode node = this.rootNode;
        // Loop through the route parts
        for (Iterator<RoutePart> iter = parts.iterator(); iter.hasNext();) {
            RoutePart part = iter.next();
            boolean isLast = iter.hasNext();

            // Attempt to find an existing child node with the
            // provided aliases
            Optional<CommandNode> childCandidate = findChild(node, part);
            if (childCandidate.isEmpty()) {
                // Create new child node
                node = part.toNode(node);
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

                childNode.mergeAliases(part.aliases);
                node = childNode;
            }
        }
    }

    /**
     * Finds a child node of the supplied parent node by primary alias
     * or secondary aliases.
     *
     * @param parent The parent node
     * @param part The route part containing the aliases
     */
    private Optional<CommandNode> findChild(CommandNode parent, RoutePart part) {
        Optional<CommandNode> found = parent.findChild(part.primaryAlias);
        if (found.isEmpty()) {
            for (String alias : part.aliases) found = parent.findChild(alias);
        }

        return found;
    }

    /**
     * Simple class storing a primary alias and a {@link java.util.Set} of
     * secondary aliases.
     */
    private record RoutePart(String primaryAlias, Set<String> aliases) {

        RoutePart {
            requireNonNull(primaryAlias, "primaryAlias cannot be null");
            requireNonNull(aliases, "aliases cannot be null");
        }

        /**
         * Parses a string representation of a route part
         * into a {@link RoutePart} instance.
         *
         * @param part The string representation
         */
        static RoutePart of(String part) {
            String[] split = part.split("\\|");
            // If split is empty, the part is empy/blank
            if (split.length == 0) {
                throw new IllegalArgumentException("'%s' is not a valid route part".formatted(part));
            }

            // The first alias will be treated as the primary
            String primaryAlias = split[0].trim();
            if (primaryAlias.isBlank()) throw new IllegalArgumentException("Primary alias cannot be blank");

            // The remaining (if any) aliases become secondary aliases
            Set<String> aliases = Arrays.stream(split).skip(1)
                    .map(String::trim)
                    .filter(String::isBlank) // Filter out blank strings
                    .collect(Collectors.toSet());

            return new RoutePart(primaryAlias, aliases);
        }

        /**
         * Creates a new {@link CommandNode} with the supplied parent
         * being the parent node and the primary and secondary aliases
         * held by this {@link RoutePart} instance.
         *
         * @param parent The parent node
         */
        public CommandNode toNode(CommandNode parent) {
            return new CommandNode(this.primaryAlias, this.aliases, parent);
        }
    }
}
