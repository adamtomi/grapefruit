package grapefruit.command.dispatcher;

import grapefruit.command.CommandContainer;
import grapefruit.command.CommandDefinition;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.CommandAuthorizationException;
import grapefruit.command.dispatcher.exception.CommandInvocationException;
import grapefruit.command.dispatcher.exception.IllegalCommandSourceException;
import grapefruit.command.dispatcher.exception.NoSuchCommandException;
import grapefruit.command.dispatcher.listener.PostDispatchListener;
import grapefruit.command.dispatcher.listener.PreDispatchListener;
import grapefruit.command.dispatcher.listener.PreProcessLitener;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.dispatcher.registration.CommandRegistrationContext;
import grapefruit.command.dispatcher.registration.CommandRegistrationHandler;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.MessageProvider;
import grapefruit.command.message.Template;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.ParameterNode;
import grapefruit.command.parameter.StandardParameter;
import grapefruit.command.parameter.modifier.Source;
import grapefruit.command.parameter.resolver.ParameterResolutionException;
import grapefruit.command.parameter.resolver.ResolverRegistry;
import grapefruit.command.util.Miscellaneous;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static grapefruit.command.parameter.ParameterNode.FLAG_PATTERN;
import static java.lang.String.format;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl<S> implements CommandDispatcher<S> {
    private static final System.Logger LOGGER = System.getLogger(CommandDispatcherImpl.class.getName());
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final ResolverRegistry<S> resolverRegistry = new ResolverRegistry<>();
    private final MethodParameterParser<S> parameterParser = new MethodParameterParser<>(this.resolverRegistry);
    private final Queue<PreProcessLitener<S>> preProcessLiteners = new ConcurrentLinkedQueue<>();
    private final Queue<PreDispatchListener<S>> preDispatchListeners = new ConcurrentLinkedQueue<>();
    private final Queue<PostDispatchListener<S>> postDispatchListeners = new ConcurrentLinkedQueue<>();
    private final Set<ExceptionHandlerWrapper<? extends Throwable>> exceptionHandlers = Collections.synchronizedSet(new HashSet<>());
    private final TypeToken<S> commandSourceType;
    private final CommandAuthorizer<S> commandAuthorizer;
    private final CommandGraph<S> commandGraph;
    private final Executor sameThreadExecutor = Runnable::run;
    private final Executor asyncExecutor;
    private final MessageProvider messageProvider;
    private final CommandRegistrationHandler<S> registrationHandler;

    protected CommandDispatcherImpl(final @NotNull TypeToken<S> commandSourceType,
                                    final @NotNull CommandAuthorizer<S> commandAuthorizer,
                                    final @NotNull Executor asyncExecutor,
                                    final @NotNull MessageProvider messageProvider,
                                    final @NotNull CommandRegistrationHandler<S> registrationHandler) {
        this.commandSourceType = requireNonNull(commandSourceType, "commandSourceType cannot be null");
        this.commandAuthorizer = requireNonNull(commandAuthorizer, "commandAuthorizer cannot be null");
        this.asyncExecutor = requireNonNull(asyncExecutor, "asyncExecutor cannot be null");
        this.commandGraph = new CommandGraph<>(this.commandAuthorizer);
        this.messageProvider = requireNonNull(messageProvider, "messageProvider cannot be null");
        this.registrationHandler = requireNonNull(registrationHandler, "registrationHandler cannot be null");
    }

    @Override
    public @NotNull TypeToken<S> commandSourceType() {
        return this.commandSourceType;
    }

    @Override
    public @NotNull ResolverRegistry<S> resolvers() {
        return this.resolverRegistry;
    }

    @Override
    public @NotNull MessageProvider messageProvider() {
        return this.messageProvider;
    }

    @Override
    public void registerListener(final @NotNull PreProcessLitener<S> listener) {
        this.preProcessLiteners.offer(requireNonNull(listener, "listener cannot be null"));
    }

    @Override
    public void registerListener(final @NotNull PreDispatchListener<S> listener) {
        this.preDispatchListeners.offer(requireNonNull(listener, "listener cannot be null"));
    }

    @Override
    public void registerListener(final @NotNull PostDispatchListener<S> listener) {
        this.postDispatchListeners.offer(requireNonNull(listener, "listener cannot be null"));
    }

    @Override
    public <X extends Throwable> void handle(final @NotNull Class<X> clazz, final @NotNull Consumer<X> handler) {
        this.exceptionHandlers.add(new ExceptionHandlerWrapper<>(clazz, handler));
    }

    @Override
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
            final @Nullable TypeToken<?> commandSourceType = requiresCommandSource
                    ? TypeToken.get(method.getParameters()[0].getType())
                    : null;

            if (commandSourceType != null) {
                final Class<?> baseCommandSourceType = GenericTypeReflector.erase(this.commandSourceType.getType());
                final Class<?> foundCommandSourceType = GenericTypeReflector.erase(commandSourceType.getType());
                if (!baseCommandSourceType.isAssignableFrom(foundCommandSourceType)) {
                    throw new IllegalArgumentException(format("Invalid command source type detected (%s)! Must implement %s",
                            foundCommandSourceType.getName(), baseCommandSourceType.getName()));
                }
            }

            final MethodHandle methodHandle = this.lookup.unreflect(method).bindTo(container);
            final CommandRegistration<S> reg = new CommandRegistration<>(
                    methodHandle,
                    parameters,
                    permission,
                    commandSourceType,
                    requiresCommandSource,
                    runAsync);

            this.commandGraph.registerCommand(route, reg);
            final CommandRegistrationContext<S> regContext = new CommandRegistrationContext<>(Arrays.asList(route.split(" ")), reg);
            this.registrationHandler.accept(regContext);
        } catch (final MethodParameterParser.RuleViolationException ex) {
            throw new RuntimeException(ex);
        } catch (final Throwable ex) {
            throw new RuntimeException("Could not register command", ex);
        }
    }

    @Override
    public void dispatchCommand(final @NotNull S source, final @NotNull String commandLine) {
        requireNonNull(source, "source cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        for (final PreProcessLitener<S> listener : this.preProcessLiteners) {
            try {
                if (!listener.onPreProcess(source, commandLine)) {
                    return;
                }
            } catch (final Throwable ex) {
                LOGGER.log(WARNING, format("PreProcessListener %s threw an exception", listener.getClass()));
            }
        }

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

                for (final PreDispatchListener<S> listener : this.preDispatchListeners) {
                    try {
                        if (!listener.onPreDispatch(source, commandLine, reg)) {
                            return;
                        }
                    } catch (final Throwable ex) {
                        LOGGER.log(WARNING, format("PreDispatchListener %s threw an exception", listener.getClass()));
                    }
                }

                final Executor executor = reg.runAsync()
                        ? this.asyncExecutor
                        : this.sameThreadExecutor;
                executor.execute(() -> {
                    try {
                        final CommandResult commandResult = dispatchCommand(reg, commandLine, source, args);
                        this.postDispatchListeners.forEach(listener -> {
                            try {
                                listener.onPostDispatch(source, commandResult);
                            } catch (final Throwable ex) {
                                LOGGER.log(WARNING, format("PostDispatchListener %s threw an exception", listener.getClass()));
                            }
                        });

                    } catch (final CommandException ex) {
                        handle(ex);
                    }
                });
            } else {
                throw new NoSuchCommandException(args.element().rawArg(), commandLine);
            }
        } catch (final CommandException ex) {
            handle(ex);
        }
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
                            throw new CommandException(Message.of(
                                    MessageKeys.UNRECOGNIZED_COMMAND_FLAG,
                                    Template.of("{value}", rawInput)
                            ));
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
                            throw new CommandException(Message.of(MessageKeys.TOO_MANY_ARGUMENTS));
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
            throw new CommandInvocationException(ex, commandLine);
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
                throw new CommandException(Message.of(MessageKeys.TOO_FEW_ARGUMENTS));
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
            if (reg.commandSourceType() == null) {
                throw new AssertionError("Well, this is kind of unexpected");
            }

            // Validate the type of the command source
            final Class<?> foundCommandSourceType = source.getClass();
            final Class<?> requiredCommandSourceType = GenericTypeReflector.erase(reg.commandSourceType().getType());
            if (!requiredCommandSourceType.isAssignableFrom(foundCommandSourceType)) {
                throw new IllegalCommandSourceException(requiredCommandSourceType, foundCommandSourceType);
            }

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

    @Override
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

    @SuppressWarnings("unchecked")
    private <X extends Throwable> void handle(final @NotNull X ex) {
        final Class<X> clazz = (Class<X>) ex.getClass();
        boolean found = false;
        for (final ExceptionHandlerWrapper<? extends Throwable> wrapper : this.exceptionHandlers) {
            if (wrapper.exceptionType().isAssignableFrom(clazz)) {
                final Consumer<X> handler = (Consumer<X>) wrapper.handler();
                handler.accept(ex);
                found = true;
            }
        }

        if (!found) {
            ex.printStackTrace();
        }
    }

    private static final record ExceptionHandlerWrapper<X extends Throwable> (@NotNull Class<X> exceptionType, @NotNull Consumer<X> handler) {}
}
