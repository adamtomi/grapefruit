package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

final class CommandGraph<S> {
    private static final String ALIAS_SEPARATOR = "\\|";
    private final CommandNode<S> rootNode = new CommandNode<>("__ROOT__", Set.of(), null);

    public void registerCommand(final @NotNull String route, final @NotNull CommandRegistration<S> reg) {
        final List<RouteFragment> parts = Arrays.stream(route.split(" "))
                .map(String::trim)
                .map(x -> x.split(ALIAS_SEPARATOR))
                .map(x -> {
                    final String primary = x[0];
                    final String[] aliases = x.length > 1
                            ? Arrays.copyOfRange(x, 1, x.length)
                            : new String[0];
                    return new RouteFragment(primary, aliases);
                })
                .collect(Collectors.toList());

        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Empty command tree detected");
        }

        CommandNode<S> node = this.rootNode;
        for (final Iterator<RouteFragment> iter = parts.iterator(); iter.hasNext();) {
            final RouteFragment current = iter.next();
            final boolean last = !iter.hasNext();
            final CommandNode<S> childNode = new CommandNode<S>(current.primary(), current.aliases(), !last ? null : reg);
            final Optional<CommandNode<S>> possibleChild = node.findChild(childNode.primary());

            if (possibleChild.isPresent()) {
                if (last) {
                    throw new IllegalStateException("Ambigious command tree");
                }

                final CommandNode<S> realChildNode = possibleChild.get();
                realChildNode.mergeAliases(childNode.aliases());
                node = realChildNode;
            } else {
                node.addChild(childNode);
                node = childNode;
            }
        }
    }

    public @NotNull Optional<CommandRegistration<S>> routeCommand(final @NotNull Queue<String> args) {
        CommandNode<S> commandNode = this.rootNode;
        String arg;
        while ((arg = args.poll()) != null) {
            Optional<CommandNode<S>> possibleChild = commandNode.findChild(arg);
            if (possibleChild.isEmpty()) {
                for (final CommandNode<S> each : commandNode.children()) {
                    if (each.aliases().contains(arg)) {
                        possibleChild = Optional.of(each);
                    }
                }

                if (possibleChild.isEmpty()) {
                    return Optional.empty();
                }
            }

            final CommandNode<S> child = possibleChild.get();
            if (child.children().isEmpty()) {
                return child.registration();
            } else {
                commandNode = child;
            }
        }

        return Optional.empty();
    }

    public @NotNull List<String> listSuggestions(final @NotNull String[] args) {
        return List.of();
    }

    private static final record RouteFragment (@NotNull String primary, @NotNull String[] aliases) {}
}
