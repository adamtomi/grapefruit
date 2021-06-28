package grapefruit.command.dispatcher;

import grapefruit.command.CommandContainer;
import grapefruit.command.CommandDefinition;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.CommandAuthorizationException;
import grapefruit.command.dispatcher.exception.NoSuchCommandException;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.ParameterNode;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.modifier.Source;
import grapefruit.command.parameter.resolver.ParameterResolutionException;
import grapefruit.command.parameter.resolver.ResolverRegistry;
import grapefruit.command.util.Miscellaneous;
import io.leangen.geantyref.GenericTypeReflector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static grapefruit.command.parameter.ParameterNode.FLAG_PATTERN;
import static java.lang.String.format;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

public final class CommandDispatcher<S> {
    private static final System.Logger LOGGER = System.getLogger(CommandDispatcher.class.getName());
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final ResolverRegistry<S> resolverRegistry = new ResolverRegistry<>();
    private final MethodParameterParser<S> parameterParser = new MethodParameterParser<>(this.resolverRegistry);
    private final CommandAuthorizer<S> commandAuthorizer;
    private final CommandGraph<S> commandGraph;
    private final Executor sameThreadExecutor = Runnable::run;
    private final Executor asyncExecutor;

    private CommandDispatcher(final @NotNull CommandAuthorizer<S> commandAuthorizer,
                              final @NotNull Executor asyncExecutor) {
        this.commandAuthorizer = requireNonNull(commandAuthorizer, "commandAuthorizer cannot be null");
        this.asyncExecutor = requireNonNull(asyncExecutor, "asyncExecutor cannot be null");
        this.commandGraph = new CommandGraph<>(this.commandAuthorizer);
    }

    public @NotNull ResolverRegistry<S> resolvers() {
        return this.resolverRegistry;
    }

    public void registerCommands(final @NotNull CommandContainer container) {
        requireNonNull(container, "container cannot be null");
        for (final Method method : container.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(CommandDefinition.class)) {
                continue;
            }

            registerCommand(container, method);
        }
    }

    private void registerCommand(final @NotNull CommandContainer container,
                                 final @NotNull Method method) {
        final CommandDefinition def = method.getAnnotation(CommandDefinition.class);
        if (def == null) {
            throw new IllegalStateException(format("Missing @CommandDefinition annotation on %s", method.getName()));
        }

        final String route = def.route();
        final @Nullable String permission = Miscellaneous.emptyToNull(def.permission());
        final boolean runAsync = def.runAsync();

        try {
            final List<ParameterNode<S>> parameters = this.parameterParser.collectParameters(method);
            final boolean requiresCommandSource = !parameters.isEmpty()
                    && method.getParameters()[0].isAnnotationPresent(Source.class);
            final MethodHandle methodHandle = this.lookup.unreflect(method).bindTo(container);
            final CommandRegistration<S> reg = new CommandRegistration<>(
                    methodHandle,
                    parameters,
                    permission,
                    requiresCommandSource,
                    runAsync);

            this.commandGraph.registerCommand(route, reg);
        } catch (final MethodParameterParser.RuleViolationException ex) {
            ex.printStackTrace();
        } catch (final Throwable ex) {
            throw new RuntimeException("Could not register command", ex);
        }
    }

    public CompletableFuture<CommandResult> dispatchCommand(final @NotNull S source,
                                                            final @NotNull String commandLine) {
        requireNonNull(source, "source cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        final CompletableFuture<CommandResult> resultFuture = new CompletableFuture<>();
        try {
            final Queue<CommandArgument> args = Arrays.stream(commandLine.split(" "))
                    .map(String::trim)
                    .map(CommandArgument::new)
                    .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
            final Optional<CommandRegistration<S>> registrationOpt = this.commandGraph.routeCommand(args);
            if (registrationOpt.isPresent()) {
                final CommandRegistration<S> reg = registrationOpt.get();
                final @Nullable String permission = reg.permission();

                if (!Miscellaneous.checkAuthorized(source, permission, this.commandAuthorizer)) {
                    throw new CommandAuthorizationException(permission);
                }

                final Executor executor = reg.runAsync()
                        ? this.asyncExecutor
                        : this.sameThreadExecutor;
                executor.execute(() -> {
                    try {
                        final CommandResult commandResult = dispatchCommand(reg, commandLine, source, args);
                        resultFuture.complete(commandResult);
                    } catch (final CommandException ex) {
                        resultFuture.completeExceptionally(ex);
                    }
                });
            } else {
                throw new NoSuchCommandException(args.element().rawArg(), commandLine);
            }
        } catch (final CommandException ex) {
            resultFuture.completeExceptionally(ex);
        }

        return resultFuture;
    }

    private @NotNull CommandResult dispatchCommand(final @NotNull CommandRegistration<S> registration,
                                                   final @NotNull String commandLine,
                                                   final @NotNull S source,
                                                   final @NotNull Queue<CommandArgument> args) throws CommandException {
        final CommandResult result = new CommandResult(commandLine, preprocessArguments(registration.parameters()));
        try {
            int parameterIndex = 0;
            CommandArgument input;
            while ((input = args.peek()) != null) {
                try {
                    final String rawInput = input.rawArg();
                    final Matcher matcher = FLAG_PATTERN.matcher(rawInput);
                    if (matcher.matches()) {
                        final String flagName = matcher.group(1).toLowerCase(Locale.ROOT);
                        final Optional<ParameterNode<S>> flagParameterOpt = registration.parameters()
                                .stream()
                                .filter(param -> param instanceof StandardParameter.ValueFlag
                                        || param instanceof StandardParameter.PresenceFlag)
                                .filter(param -> param.name().equals(flagName))
                                .findFirst();

                        if (flagParameterOpt.isEmpty()) {
                            throw new CommandException(format("Unrecognized flag: %s", flagName));
                        }

                        input.markConsumed();
                        args.remove();
                        final ParameterNode<S> flagParameter = flagParameterOpt.get();
                        final ParsedCommandArgument parsedArg = result.findArgumentUnchecked(flagName);
                        if (flagParameter instanceof StandardParameter.PresenceFlag) {
                            parsedArg.parsedValue(true);

                        } else {
                            final Object parsedValue = flagParameter.resolver().resolve(source, args, flagParameter.unwrap());
                            parsedArg.parsedValue(parsedValue);
                        }

                    } else {
                        final List<ParameterNode<S>> parameters = registration.parameters();
                        if (parameterIndex >= parameters.size()) {
                            throw new CommandException("Too many arguments!");
                        }

                        final ParameterNode<S> parameter = parameters.get(parameterIndex);
                        final Object parsedValue = parameter.resolver().resolve(source, args, parameter.unwrap());
                        final ParsedCommandArgument parsedArg = result.findArgumentAt(parameterIndex);
                        parsedArg.parsedValue(parsedValue);
                        parameterIndex++;
                    }

                    if (!args.isEmpty()) {
                        args.element().markConsumed();
                    }
                } catch (final ParameterResolutionException ex) {
                    final CommandParameter parameter = ex.parameter();
                    if (!parameter.isOptional()) {
                        throw ex;
                    }
                } finally {
                    if (!args.isEmpty() && args.element().isConsumed()) {
                        args.remove();
                    }
                }
            }

            dispatchCommand0(registration, source, postprocessArguments(result, registration.parameters()));
            return result;
        } catch (final Throwable ex) {
            throw new CommandException(ex);
        }
    }

    private @NotNull List<ParsedCommandArgument> preprocessArguments(final @NotNull Collection<ParameterNode<S>> parameters) {
        return parameters.stream()
                .map(parameter -> {
                    final CommandParameter cmdParam = parameter.unwrap();
                    final String name = parameter.name();
                    final ParsedCommandArgument parsedArg = new ParsedCommandArgument(name);

                    if (cmdParam.isOptional()) {
                        parsedArg.parsedValue(Miscellaneous.nullToPrimitive(GenericTypeReflector.erase(cmdParam.type().getType())));
                    } else if (parameter instanceof StandardParameter.PresenceFlag) {
                        parsedArg.parsedValue(false);
                    }

                    return parsedArg;
                })
                .collect(Collectors.toList());
    }

    private @NotNull List<Object> postprocessArguments(final @NotNull CommandResult result,
                                                       final @NotNull List<ParameterNode<S>> params) throws CommandException {
        final List<Object> objects = new ArrayList<>();
        for (final ParameterNode<S> param : params) {
            final ParsedCommandArgument parsedArg = result.findArgumentUnchecked(param.name());
            if (!param.unwrap().isOptional() && parsedArg.parsedValue().isEmpty()) {
                throw new CommandException(format("No value found for paremeter %s", param.name()));
            }

            objects.add(parsedArg.parsedValue().orElse(null));
        }

        return objects;
    }

    private void dispatchCommand0(final @NotNull CommandRegistration<S> reg,
                                  final @NotNull S source,
                                  final @NotNull Collection<Object> args) throws Throwable {
        final Object[] finalArgs;
        if (reg.requiresCommandSource()) {
            finalArgs = new Object[args.size() + 1];
            finalArgs[0] = source;
            int idx = 1;
            for (final Object arg : args) {
                finalArgs[idx++] = arg;
            }

        } else {
            finalArgs = args.toArray(Object[]::new);
        }

        reg.methodHandle().invokeWithArguments(finalArgs);
    }

    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String commandLine) {
        requireNonNull(source, "source cannot be null");
        final Deque<CommandArgument> args = Arrays.stream(commandLine.split(" "))
                .map(String::trim)
                .map(CommandArgument::new)
                .collect(Collectors.toCollection(ArrayDeque::new));
        if (args.size() == 0) {
            return List.of();
        }

        if (commandLine.charAt(commandLine.length() - 1) == ' ') {
            args.add(new CommandArgument("")); // This is a bit hacky
        }

        final String last = args.getLast().rawArg();
        return this.commandGraph.listSuggestions(source, args)
                .stream()
                .filter(x -> Miscellaneous.startsWithIgnoreCase(x, last))
                .collect(Collectors.toList());
    }

    public static <S> @NotNull Builder<S> builder() {
        return new Builder<>();
    }

    public static final class Builder<S> {
        private CommandAuthorizer<S> commandAuthorizer;
        private Executor asyncExecutor;

        private Builder() {}

        public @NotNull Builder<S> withAuthorizer(final @NotNull CommandAuthorizer<S> commandAuthorizer) {
            this.commandAuthorizer = requireNonNull(commandAuthorizer, "commandAuthorizer cannot be null");
            return this;
        }

        public @NotNull Builder<S> withAsyncExecutor(final @NotNull Executor asyncExecutor) {
            this.asyncExecutor = requireNonNull(asyncExecutor, "asyncExecutor cannot be null");
            return this;
        }

        @SuppressWarnings("unchecked")
        public @NotNull CommandDispatcher<S> build() {
            final CommandAuthorizer<S> authorizer;
            if (this.commandAuthorizer == null) {
                LOGGER.log(WARNING, "No CommandAuthorizer was specified, defaulting to noop implementation");
                authorizer = (CommandAuthorizer<S>) CommandAuthorizer.NO_OP;
            } else {
                authorizer = this.commandAuthorizer;
            }

            final Executor asyncExecutor;
            if (this.asyncExecutor == null) {
                LOGGER.log(WARNING, "No async Executor was specified, defaulting to Executors#newCachedThreadPool");
                asyncExecutor = Executors.newCachedThreadPool();
            } else {
                asyncExecutor = this.asyncExecutor;
            }

            return new CommandDispatcher<>(authorizer, asyncExecutor);
        }
    }
}
