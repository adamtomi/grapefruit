package grapefruit.command.dispatcher.tree;

import java.util.Set;

import static java.util.Objects.requireNonNull;

record RouteNodeImpl(String primaryAlias, Set<String> secondaryAliases) implements RouteNode {
    RouteNodeImpl {
        requireNonNull(primaryAlias, "primaryAliases cannot be null");
        requireNonNull(secondaryAliases, "secondaryAliases cannot be null");
    }
}
