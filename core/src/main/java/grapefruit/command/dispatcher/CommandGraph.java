package grapefruit.command.dispatcher;

import grapefruit.command.parameter.ParameterNode;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.resolver.ParameterResolutionException;
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
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static grapefruit.command.parameter.ParameterNode.FLAG_PATTERN;
import static java.util.Objects.requireNonNull;

final class CommandGraph<S> {
    protected static final String ALIAS_SEPARATOR = "\\|";
    private final CommandNode<S> rootNode = new CommandNode<>("__ROOT__", Set.of(), null);
    private final CommandAuthorizer<S> authorizer;

    CommandGraph(final @NotNull CommandAuthorizer<S> authorizer) {
        this.authorizer = requireNonNull(authorizer, "authorizer cannot be null");
    }

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

    public @NotNull List<String> listSuggestions(final @NotNull S source, final @NotNull Deque<CommandArgument> args) {
        CommandNode<S> commandNode = this.rootNode;
        CommandArgument part;
        while ((part = args.peek()) != null) {
            if (commandNode.registration().isPresent()) {
                break;

            } else {
                final String rawArg = part.rawArg();
                Optional<CommandNode<S>> childNode = commandNode.findChild(rawArg);
                if (childNode.isPresent()) {
                    commandNode = childNode.get();

                } else {
                    for (final CommandNode<S> each : commandNode.children()) {
                        if (each.aliases().contains(rawArg)) {
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
        final Deque<CommandArgument> argsCopy = new ConcurrentLinkedDeque<>(args);
        if (registrationOpt.isPresent()) {
            final CommandRegistration<S> registration = registrationOpt.orElseThrow();
            if (!Miscellaneous.checkAuthorized(source, registration.permission(), this.authorizer)) {
                return List.of();
            }

            for (final ParameterNode<S> param : registration.parameters()) {
                if (args.isEmpty()) {
                    return List.of();
                }

                CommandArgument currentArg = args.element();
                final Matcher flagPatternMatcher = FLAG_PATTERN.matcher(currentArg.rawArg());
                if (flagPatternMatcher.matches()) {
                    args.remove();
                    if (args.isEmpty()) {
                        return suggestFor(source, param, args, currentArg.rawArg());
                    }

                    currentArg = args.element();
                }

                if (currentArg.rawArg().equals("") || args.size() < 2) {
                    return suggestFor(source, param, argsCopy, currentArg.rawArg());
                }

                try {
                    param.resolver().resolve(source, args, param.unwrap());
                    args.remove();
                } catch (final ParameterResolutionException ex) {
                    return List.of();
                }
            }

            return List.of();

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

    private @NotNull List<String> suggestFor(final @NotNull S source,
                                             final @NotNull ParameterNode<S> parameter,
                                             final @NotNull Deque<CommandArgument> previousArgs,
                                             final @NotNull String currentArg) {
        if (parameter instanceof StandardParameter.ValueFlag) {
            if (previousArgs.stream().anyMatch(arg -> arg.rawArg().equalsIgnoreCase(Miscellaneous.formatFlag(parameter.name())))) {
                return parameter.resolver().listSuggestions(source, currentArg, parameter.unwrap());
            }

            return List.of(Miscellaneous.formatFlag(parameter.name()));
        }

        return parameter.resolver().listSuggestions(source, currentArg, parameter.unwrap());
    }

    private static final record RouteFragment (@NotNull String primary, @NotNull String[] aliases) {}
}
