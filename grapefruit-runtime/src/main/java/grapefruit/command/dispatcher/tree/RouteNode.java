package grapefruit.command.dispatcher.tree;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command routes are composed of nodes, each node having a primary alias
 * and optionally multiple secondary aliases.
 */
public interface RouteNode {
    /* Aliases are expected to be separated by this character. */
    String ALIAS_SEPARATOR = "\\|";

    /**
     * Returns the primary alias associated with this node.
     *
     * @return The primary alias
     */
    String primaryAlias();

    /**
     * Returns an immutable set of secondary aliases. If no
     * secondary aliases are present, an empty list is returned.
     *
     * @return The secondary aliases
     */
    Set<String> secondaryAliases();

    /**
     * Parses the provided {@link String} representation of a
     * route node into a {@link RouteNode} instance.
     *
     * @param node The route part to parse
     * @return The created route node
     */
    static RouteNode of(String node) {
        String[] split = node.split(ALIAS_SEPARATOR);
        // If split is empty, the part is empy/blank
        if (split.length == 0) {
            throw new IllegalArgumentException("'%s' is not a valid route part".formatted(node));
        }

        // The first alias will be treated as the primary
        String primaryAlias = split[0].trim();
        if (primaryAlias.isBlank()) throw new IllegalArgumentException("Primary alias cannot be blank");

        // The remaining (if any) aliases become secondary aliases
        Set<String> aliases = Arrays.stream(split).skip(1)
                .map(String::trim)
                .filter(x -> !x.isBlank()) // Filter out blank strings
                .collect(Collectors.toSet());

        return new RouteNodeImpl(primaryAlias, aliases);
    }

    /**
     * Parses the provided {@link String} representation of a
     * command route, and turns into an immutable list of
     * {@link RouteNode nodes}.
     *
     * @param route The route as a string
     * @return The parsed nodes
     */
    static List<RouteNode> parse(String route) {
        /*
         * Split the command route into parts. Each individual part
         * will become a command node in the tree if the registration
         * succeeds.
         */
        return Arrays.stream(route.split(" "))
                .map(RouteNode::of)
                .toList();
    }
}
