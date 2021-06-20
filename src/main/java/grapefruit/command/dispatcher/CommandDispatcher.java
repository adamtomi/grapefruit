package grapefruit.command.dispatcher;

import grapefruit.command.CommandContainer;
import grapefruit.command.CommandDefinition;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.CommandAuthorizationException;
import grapefruit.command.dispatcher.exception.NoSuchCommandException;
import grapefruit.command.parameter.modifier.Source;
import grapefruit.command.parameter.resolver.ResolverRegistry;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

public final class CommandDispatcher<S> {
    private static final System.Logger LOGGER = System.getLogger(CommandDispatcher.class.getName());
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
            final Queue<String> args = Arrays.stream(commandLine.split(" "))
                    .map(String::trim)
                    .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
            final Optional<CommandRegistration<S>> registrationOpt = this.commandGraph.routeCommand(args);
            if (registrationOpt.isPresent()) {
                final CommandRegistration<S> reg = registrationOpt.get();
                final @Nullable String permission = reg.permission();

                if (permission != null && !this.commandAuthorizer.isAuthorized(source, permission)) {
                    throw new CommandAuthorizationException(permission);
                }

                if (args.size() < reg.parameters().size()) {
                    throw new CommandException("Too few arguments specified");
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
                throw new NoSuchCommandException(args.element(), commandLine);
            }
        } catch (final CommandException ex) {
            result.completeExceptionally(ex);
        }

        return result;
    }

    private void dispatchCommand(final @NotNull CommandRegistration<S> registration,
                                 final @NotNull S source,
                                 final @NotNull Queue<String> args) throws CommandException {
        try {
            final Set<Object> objects = new LinkedHashSet<>();
            for (final ParameterNode<S> parameter : registration.parameters()) {
                objects.add(parameter.resolver().resolve(source, args, parameter.parameter()));
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
