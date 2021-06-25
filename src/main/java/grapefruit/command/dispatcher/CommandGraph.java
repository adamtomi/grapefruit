package grapefruit.command.dispatcher;

import grapefruit.command.parameter.ParameterNode;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
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
            final CommandNode<S> childNode = new CommandNode<>(current.primary(), current.aliases(), !last ? null : reg);
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

    public @NotNull Optional<CommandRegistration<S>> routeCommand(final @NotNull Queue<CommandArgument> args) {
        CommandNode<S> commandNode = this.rootNode;
        CommandArgument arg;
        while ((arg = args.poll()) != null) {
            final String rawInput = arg.rawArg();
            Optional<CommandNode<S>> possibleChild = commandNode.findChild(rawInput);
            if (possibleChild.isEmpty()) {
                for (final CommandNode<S> each : commandNode.children()) {
                    if (each.aliases().contains(rawInput)) {
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

    public @NotNull List<String> listSuggestions(final @NotNull S source, final @NotNull Deque<String> args) {
        CommandNode<S> commandNode = this.rootNode;
        String part;
        while ((part = args.peek()) != null) {
            if (commandNode.registration().isPresent()) {
                break;

            } else {
                Optional<CommandNode<S>> childNode = commandNode.findChild(part);
                if (childNode.isPresent()) {
                    commandNode = childNode.get();

                } else {
                    for (final CommandNode<S> each : commandNode.children()) {
                        if (each.aliases().contains(part)) {
                            childNode = Optional.of(each);
                        }
                    }
                }

                if (childNode.isEmpty()) {
                    break;
                }
            }

            args.remove();
        }

        final Optional<CommandRegistration<S>> registrationOpt = commandNode.registration();
        if (registrationOpt.isPresent()) {
            /*final int lastParameterIndex = args.size() - 1;
            final CommandRegistration<S> registration = registrationOpt.get();
            final String lastArgument = args.pollLast();
            if (registration.parameters().size() <= lastParameterIndex
                    || lastArgument == null) {
                return List.of();
            }*/
            final CommandRegistration<S> registration = registrationOpt.orElseThrow();
            final int argCount = args.size();
            final int paramIndex = argCount >= registration.parameters().size()
                    ? registration.parameters().size() - 1
                    : argCount - 1;
            final String lastArgument = args.pollLast();
            if (lastArgument == null) {
                System.out.println("empty list");
                return List.of();
            }

            System.out.println(paramIndex);
            System.out.println(argCount);
            System.out.println(lastArgument);
            final ParameterNode<S> lastParameter = registration.parameters().get(paramIndex);
            System.out.println(lastParameter);
            if (lastParameter instanceof StandardParameter.ValueFlag) {
                final String flagName = Miscellaneous.formatFlag(lastParameter.name());
                System.out.println("valueflag");
                System.out.println(flagName);
                if (args.contains(flagName)) {
                    System.out.println("contains");
                    System.out.println(args);
                    return lastParameter.resolver().listSuggestions(source, lastArgument, lastParameter.unwrap());
                }

                System.out.println("doesnt contain");
                return List.of(flagName);
            }

            return lastParameter.resolver().listSuggestions(source, lastArgument, lastParameter.unwrap());

        } else {
            return commandNode.children().stream()
                    .map(x -> {
                        final List<String> suggestions = new ArrayList<>();
                        suggestions.add(x.primary());
                        suggestions.addAll(x.aliases());
                        return suggestions;
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private static final record RouteFragment (@NotNull String primary, @NotNull String[] aliases) {}
}
