package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.dispatcher.registration.RedirectingCommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.parameter.mapper.ParameterMappingException;
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
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static grapefruit.command.parameter.FlagParameter.FLAG_PATTERN;
import static grapefruit.command.util.Miscellaneous.formatFlag;
import static java.util.Objects.requireNonNull;

final class CommandGraph<S> {
    protected static final String ALIAS_SEPARATOR = "\\|";
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

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                                 final @NotNull Deque<CommandInput> args) {
        System.out.println("listSuggestions");
        final S source = context.source();
        CommandNode<S> commandNode = this.rootNode;
        CommandInput part;
        System.out.println(context);
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

        final Optional<CommandRegistration<S>> registrationOpt = commandNode.registration();
        final Deque<CommandInput> argsCopy = new ConcurrentLinkedDeque<>(args);
        if (registrationOpt.isPresent()) {
            System.out.println("registration is present");
            final CommandRegistration<S> registration = registrationOpt.orElseThrow();
            if (!Miscellaneous.checkAuthorized(source, registration.permission().orElse(null), this.authorizer)) {
                return List.of();
            }

            final List<CommandParameter<S>> parameters = registration.parameters();
            final List<FlagParameter<S>> flags = parameters.stream()
                    .filter(CommandParameter::isFlag)
                    .map(x -> (FlagParameter<S>) x)
                    .toList();
            System.out.println("entering for loop");
            for (final CommandParameter<S> param : parameters) {
                if (args.isEmpty()) {
                    return List.of();
                }

                CommandInput input = args.element();
                final String rawInput = input.rawArg();
                System.out.println(rawInput);
                if (rawInput.startsWith("-")) {
                    final Matcher flagPatternMatcher = FLAG_PATTERN.matcher(rawInput);
                    if (flagPatternMatcher.matches()) {
                        System.out.println("is flag");
                        try {
                            System.out.println("creating flag group");
                            final FlagGroup<S> flagGroup = FlagGroup.parse(rawInput, flagPatternMatcher, parameters);
                            System.out.println("done");
                            args.remove();
                            if (args.isEmpty()) {
                                System.out.println("args.isEmpty");
                                return suggestFor(context, flagGroup.iterator().next(), args, rawInput);
                            }
                        } catch (final CommandException ignored) {
                            System.out.println("error creating flag groups");
                        }

                        System.out.println("not empty");
                        input = args.element();
                    } else {
                        System.out.println("starts with - but not a flag yet");
                        if (!param.type().isSubtypeOf(Miscellaneous.numberType())) {
                            System.out.println("param is not of type number");
                            final List<String> flagOptions = new ArrayList<>();
                            System.out.println("collecting all flags that don't have a value");
                            for (final FlagParameter<S> flag : flags) {
                                final Optional<Object> storedValue = context.find(flag.flagName());
                                if (storedValue.isEmpty() || storedValue.map(Boolean.TYPE::cast).orElse(false)) {
                                    flagOptions.addAll(collectFlagOptions(flag));
                                }
                            }

                            System.out.println("returning flagOPtions");
                            return flagOptions;
                        }
                    }
                }

                if (input instanceof BlankCommandInput || args.size() < 2) {
                    System.out.println("blank input || args < 2");
                    return suggestFor(context, param, argsCopy, "");
                }

                if (param.mapper().suggestionsNeedValidation()) {
                    try {
                        param.mapper().map(context, args, param.modifiers());
                    } catch (final ParameterMappingException ex) {
                        return List.of();
                    }
                }

                args.remove();
            }

            return List.of();

        } else {
            System.out.println("not present, returning children");
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

    private @NotNull List<String> suggestFor(final @NotNull CommandContext<S> context,
                                             final @NotNull CommandParameter<S> parameter,
                                             final @NotNull Deque<CommandInput> previousArgs,
                                             final @NotNull String currentArg) {
        System.out.println("suggestFor");
        System.out.println(context);
        System.out.println(parameter);
        System.out.println(previousArgs);
        System.out.println("\"" + currentArg + "\"");
        if (parameter.isFlag() && !parameter.type().equals(FlagParameter.PRESENCE_FLAG_TYPE)) {
            System.out.println("flag && !presence");
            final FlagParameter<S> flag = (FlagParameter<S>) parameter;
            if (previousArgs.stream().anyMatch(input -> Miscellaneous.containsIgnoreCase(input.rawArg(), collectFlagOptions(flag)))) {
                return parameter.mapper().listSuggestions(context, currentArg, parameter.modifiers());
            }

            System.out.println("returnin formatted flag");
            return collectFlagOptions(flag);
        }

        System.out.println("returning regular suggestions");
        return parameter.mapper().listSuggestions(context, currentArg, parameter.modifiers());
    }

    private @NotNull List<String> collectFlagOptions(final @NotNull FlagParameter<S> flag) {
        return List.of(Miscellaneous.formatFlag(flag.flagName()), Miscellaneous.formatFlag(String.valueOf(flag.shorthand())));
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
