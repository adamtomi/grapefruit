package grapefruit.command.dispatcher;

import grapefruit.command.CommandContainer;
import grapefruit.command.CommandDefinition;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.CommandAuthorizationException;
import grapefruit.command.dispatcher.exception.NoSuchCommandException;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.modifier.Flag;
import grapefruit.command.parameter.modifier.OptParam;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

public final class CommandDispatcher<S> {
    private static final System.Logger LOGGER = System.getLogger(CommandDispatcher.class.getName());
    private static final Pattern FLAG_PATTERN = Pattern.compile("^--(.+)$");
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final CommandGraph<S> commandGraph = new CommandGraph<>();
    private final ResolverRegistry<S> resolverRegistry = new ResolverRegistry<>();
    private final MethodParameterParser<S> parameterParser = new MethodParameterParser<>(this.resolverRegistry);
    private final CommandAuthorizer<S> commandAuthorizer;
    private final Executor sameThreadExecutor = Runnable::run;
    private final Executor asyncExecutor;

    private CommandDispatcher(final @NotNull CommandAuthorizer<S> commandAuthorizer,
                              final @NotNull Executor asyncExecutor) {
        this.commandAuthorizer = requireNonNull(commandAuthorizer, "commandAuthorizer cannot be null");
        this.asyncExecutor = requireNonNull(asyncExecutor, "asyncExecutor cannot be null");
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

    public CompletableFuture<?> dispatchCommand(final @NotNull S source,
                                                final @NotNull String commandLine) {
        requireNonNull(source, "source cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        final CompletableFuture<?> result = new CompletableFuture<>();
        try {
            final Queue<CommandInput> args = Arrays.stream(commandLine.split(" "))
                    .map(String::trim)
                    .map(CommandInput::new)
                    .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
            final Optional<CommandRegistration<S>> registrationOpt = this.commandGraph.routeCommand(args);
            if (registrationOpt.isPresent()) {
                final CommandRegistration<S> reg = registrationOpt.get();
                final @Nullable String permission = reg.permission();

                if (permission != null && !this.commandAuthorizer.isAuthorized(source, permission)) {
                    throw new CommandAuthorizationException(permission);
                }

                final Executor executor = reg.runAsync()
                        ? this.asyncExecutor
                        : this.sameThreadExecutor;
                executor.execute(() -> {
                    try {
                        dispatchCommand(reg, source, args);
                        result.complete(null);
                    } catch (final CommandException ex) {
                        result.completeExceptionally(ex);
                    }
                });
            } else {
                throw new NoSuchCommandException(args.element().rawInput(), commandLine);
            }
        } catch (final CommandException ex) {
            result.completeExceptionally(ex);
        }

        return result;
    }

    private void dispatchCommand(final @NotNull CommandRegistration<S> registration,
                                 final @NotNull S source,
                                 final @NotNull Queue<CommandInput> args) throws CommandException {
        System.out.println("dispatchCommand");
        System.out.println(args);
        try {
            int parameterIndex = 0;
            final List<Object> objects = new ArrayList<>();
            // Store all parsed flags so we can check for duplicates
            final Set<String> parsedFlags = new HashSet<>();
            CommandInput input;
            while ((input = args.peek()) != null) {
                System.out.println("========================================");
                System.out.println(input);
                System.out.println(parameterIndex);
                System.out.println(objects);
                System.out.println(parsedFlags);
                System.out.println("--------------------");
                try {
                    final String rawInput = input.rawInput();
                    final Matcher matcher = FLAG_PATTERN.matcher(rawInput);
                    if (matcher.matches()) {
                        System.out.println("flag");
                        final String flagName = matcher.group(1).toLowerCase(Locale.ROOT);
                        if (parsedFlags.contains(flagName)) {
                            throw new CommandException(format("Duplicate flag values for: %s", flagName));
                        }

                        final Optional<ParameterNode<S>> flagParameterOpt = registration.parameters()
                                .stream()
                                .filter(param -> param.parameter().modifiers()
                                        .find(Flag.class)
                                        .map(annot -> annot.value().equalsIgnoreCase(flagName)).orElse(false))
                                .findFirst();
                        System.out.println(flagParameterOpt);

                        if (flagParameterOpt.isEmpty()) {
                            throw new CommandException(format("Unrecognized flag: %s", flagName));
                        }

                        System.out.println("valid flag");
                        input.markConsumed();
                        System.out.println("marked as consumed");
                        System.out.println("removing flag");
                        args.remove();
                        System.out.println("done");
                        System.out.println(args);
                        final ParameterNode<S> flagParameter = flagParameterOpt.get();
                        System.out.println(flagParameter);
                        final Object parsedValue = flagParameter.resolver().resolve(source, args, flagParameter.parameter());
                        System.out.println(parsedValue);
                        objects.add(flagParameter.parameter().index(), parsedValue);
                        parsedFlags.add(flagName);

                    } else {
                        System.out.println("not a flag");
                        final List<ParameterNode<S>> parameters = registration.parameters();
                        System.out.println(parameters.size());
                        if (parameterIndex >= parameters.size()) {
                            throw new CommandException("Too many arguments!");
                        }

                        final ParameterNode<S> parameter = parameters.get(parameterIndex);
                        System.out.println(parameter);
                        final Object parsedValue = parameter.resolver().resolve(source, args, parameter.parameter());
                        System.out.println(parsedValue);
                        objects.add(parsedValue);
                        parameterIndex++;
                    }

                    if (!args.isEmpty()) {
                        System.out.println("markConsumed");
                        args.element().markConsumed();
                    }
                    System.out.println("========================================");
                } catch (final ParameterResolutionException ex) {
                    final CommandParameter parameter = ex.parameter();
                    if (!parameter.modifiers().has(OptParam.class)) {
                        throw ex;
                    } else {
                        final Class<?> type = GenericTypeReflector.erase(parameter.type().getType());
                        objects.add(
                                type.isPrimitive() ? Miscellaneous.nullToPrimitive(type) : null
                        );
                    }
                } finally {
                    if (!args.isEmpty() && args.element().isConsumed()) {
                        args.remove();
                    }
                }
            }

            dispatchCommand0(registration, source, objects);
        } catch (final Throwable ex) {
            throw new CommandException(ex);
        }
    }

    private void dispatchCommand_old(final @NotNull CommandRegistration<S> registration,
                                 final @NotNull S source,
                                 final @NotNull Queue<CommandInput> args) throws CommandException {
        try {
            final Set<Object> objects = new LinkedHashSet<>();
            for (final ParameterNode<S> parameter : registration.parameters()) {
                try {
                    final Object parsedValue = parameter.resolver().resolve(source, args, parameter.parameter());
                    objects.add(parsedValue);
                    if (!args.isEmpty()) {
                        args.element().markConsumed();
                    }
                } catch (final NoSuchElementException | ParameterResolutionException ex) {
                    if (!parameter.parameter().modifiers().has(OptParam.class)) {
                        throw ex;
                    } else {
                        final Class<?> type = GenericTypeReflector.erase(parameter.parameter().type().getType());
                        objects.add(
                                type.isPrimitive() ? Miscellaneous.nullToPrimitive(type) : null
                        );
                    }
                } finally {
                    if (!args.isEmpty() && args.element().isConsumed()) {
                        args.remove();
                    }
                }
            }

            dispatchCommand0(registration, source, objects);
        } catch (final Throwable ex) {
            throw new CommandException(ex);
        }
    }

    private void dispatchCommand0(final @NotNull CommandRegistration<S> reg,
                                  final @NotNull S source,
                                  final @NotNull Collection<Object> args) throws Throwable {
        final Object[] finalArgs;
        if (reg.requiresCommandSource()) {
            finalArgs = new Object[args.size()+ 1];
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

    public @NotNull List<String> listSuggestions(final @NotNull S source, final @NotNull String[] args) {
        return List.of();
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

    public enum Stage {
        PRE, POST
    }
}
