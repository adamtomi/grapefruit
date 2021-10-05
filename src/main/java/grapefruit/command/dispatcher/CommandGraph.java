package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.dispatcher.registration.RedirectingCommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static grapefruit.command.util.Miscellaneous.formatFlag;
import static java.util.Objects.requireNonNull;

final class CommandGraph<S> {
    static final String ALIAS_SEPARATOR = "\\|";
    private static final UnaryOperator<String> AS_REQUIRED = arg -> '<' + arg + '>';
    private static final UnaryOperator<String> AS_OPTIONAL = arg -> '[' + arg + ']';
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
                final boolean isRedirectReg = reg instanceof RedirectingCommandRegistration;
                if (last && !isRedirectReg) {
                    throw new IllegalStateException("Ambigious command tree");
                }

                final CommandNode<S> realChildNode = possibleChild.get();
                realChildNode.mergeAliases(childNode.aliases());
                node = realChildNode;
                if (isRedirectReg) {
                    node.registration(reg);
                }
            } else {
                node.addChild(childNode);
                node = childNode;
            }
        }
    }

    public @NotNull RouteResult<S> routeCommand(final @NotNull Queue<CommandInput> args) {
        CommandNode<S> commandNode = this.rootNode;
        CommandInput arg;
        boolean firstRun = true;
        while ((arg = args.poll()) != null) {
            final String rawInput = arg.rawArg();
            // If we don't have a child with this name, throw NoSuchCommand
            if (firstRun) {
                if (findChild(this.rootNode, rawInput).isEmpty()) {
                    return RouteResult.failure(RouteResult.Failure.Reason.NO_SUCH_COMMAND);
                }

                firstRun = false;
            }

            final Optional<CommandNode<S>> childCandidate = findChild(commandNode, rawInput);
            if (childCandidate.isEmpty()) {
                return RouteResult.failure(RouteResult.Failure.Reason.INVALID_SYNTAX);
            }

            final CommandNode<S> child = childCandidate.get();
            final Optional<CommandRegistration<S>> registration = child.registration();
            final boolean redirectReg = registration.map(RedirectingCommandRegistration.class::isInstance).orElse(false);
            if (child.children().isEmpty() || (redirectReg && args.peek() == null)) {
                return registration.map(RouteResult::success).orElseGet(() ->
                        RouteResult.failure(RouteResult.Failure.Reason.INVALID_SYNTAX));
            } else {
                commandNode = child;
            }
        }

        return RouteResult.failure(RouteResult.Failure.Reason.INVALID_SYNTAX);
    }

    public @NotNull List<String> listSuggestions(final @NotNull Queue<CommandInput> args) {
        CommandNode<S> commandNode = this.rootNode;
        CommandInput part;
        while ((part = args.peek()) != null) {
            if (commandNode.registration().isPresent()) {
                break;

            } else {
                final String rawArg = part.rawArg();
                final Optional<CommandNode<S>> childNode = findChild(commandNode, rawArg);
                if (childNode.isEmpty()) {
                    break;
                } else {
                    commandNode = childNode.get();
                }
            }

            args.remove();
        }

        final List<String> result = new ArrayList<>();
        for (final CommandNode<S> child : commandNode.children()) {
            result.add(child.primary());
            result.addAll(child.aliases());
        }

        return result;
    }

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> commandContext,
                                                 final @NotNull String prevArg,
                                                 final @NotNull CommandRegistration<S> registration,
                                                 final @NotNull Queue<CommandInput> args) {
        final List<CommandParameter<S>> parameters = registration.parameters();
        final String currentArg = args.remove().rawArg();

        if (prevArg.startsWith("--")) {
            // TODO cleanup
            final Optional<FlagParameter<S>> flag = parameters.stream()
                    .filter(CommandParameter::isFlag)
                    .map(param -> (FlagParameter<S>) param)
                    .filter(param -> param.flagName().equalsIgnoreCase(prevArg.substring(2)))
                    .filter(param -> commandContext.find(param.flagName()).isEmpty())
                    .findFirst();
            return flag.map(param -> param.mapper().listSuggestions(commandContext, currentArg, param.modifiers()))
                    .orElse(List.of());
        }

        final Optional<CommandParameter<S>> firstNonFlagParameter = parameters.stream()
                .filter(param -> !param.isFlag())
                .filter(param -> commandContext.find(param.name()).isEmpty())
                .findFirst();
        final List<CommandParameter<S>> flags = parameters.stream()
                .filter(CommandParameter::isFlag)
                .filter(param -> commandContext.find(Miscellaneous.parameterName(param)).isEmpty())
                .toList();
        firstNonFlagParameter.ifPresent(flags::add);
        return flags.stream()
                .map(param -> param.mapper().listSuggestions(commandContext, currentArg, param.modifiers()))
                .flatMap(Collection::stream)
                .toList();
    }

    public @NotNull String generateSyntaxFor(final @NotNull String commandLine) {
        final String[] path = commandLine.split(" ");
        final StringJoiner joiner = new StringJoiner(" ");
        CommandNode<S> node = this.rootNode;

        for (final String pathPart : path) {
            final Optional<CommandNode<S>> child = findChild(node, pathPart);
            if (child.isPresent()) {
                joiner.add(pathPart);
                node = child.get();

            } else {
                break;
            }
        }

        final Optional<CommandRegistration<S>> registration = node.registration();
        if (registration.isPresent()) {
            for (final CommandParameter<S> parameter : registration.get().parameters()) {
                final String syntaxPart;
                if (parameter.isFlag()) {
                    final FlagParameter<?> flag = (FlagParameter<?>) parameter;
                    if (flag.type().equals(FlagParameter.PRESENCE_FLAG_TYPE)) {
                        syntaxPart = formatFlag(flag.flagName());
                    } else {
                        syntaxPart = formatFlag(flag.flagName()) + " " + flag.name();
                    }
                } else {
                    syntaxPart = parameter.name();
                }

                joiner.add(parameter.isOptional() ? AS_OPTIONAL.apply(syntaxPart) : AS_REQUIRED.apply(syntaxPart));
            }

        } else {
            final String children = node.children().stream()
                    .map(CommandNode::primary)
                    .collect(Collectors.joining("|"));
            joiner.add(AS_REQUIRED.apply(children));
        }

        return joiner.toString();
    }

    private @NotNull Optional<CommandNode<S>> findChild(final @NotNull CommandNode<S> parent,
                                                        final @NotNull String alias) {
        final Optional<CommandNode<S>> child = parent.findChild(alias);
        return child.isPresent()
                ? child
                : parent.children().stream().filter(x -> x.aliases().stream().anyMatch(alias::equalsIgnoreCase)).findFirst();
    }

    private static final record RouteFragment (@NotNull String primary, @NotNull String[] aliases) {}

    @SuppressWarnings("all") // Don't complain about generics.
    interface RouteResult<S> {

        static <S> @NotNull RouteResult<S> success(final @NotNull CommandRegistration<S> registration) {
            return new Success<>(registration);
        }

        static <S> @NotNull RouteResult<S> failure(final @NotNull Failure.Reason reason) {
            return new Failure<>(reason);
        }

        record Success<S>(@NotNull CommandRegistration<S> registration) implements RouteResult<S> {}

        record Failure<S>(@NotNull RouteResult.Failure.Reason reason) implements RouteResult<S> {

            enum Reason {
                NO_SUCH_COMMAND, INVALID_SYNTAX
            }
        }
    }
}
