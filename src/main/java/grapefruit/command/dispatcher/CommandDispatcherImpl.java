package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.CommandDefinition;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.CommandAuthorizationException;
import grapefruit.command.dispatcher.exception.CommandInvocationException;
import grapefruit.command.dispatcher.exception.CommandSyntaxException;
import grapefruit.command.dispatcher.exception.FlagDuplicateException;
import grapefruit.command.dispatcher.exception.IllegalCommandSourceException;
import grapefruit.command.dispatcher.exception.NoSuchCommandException;
import grapefruit.command.dispatcher.listener.PostDispatchListener;
import grapefruit.command.dispatcher.listener.PreDispatchListener;
import grapefruit.command.dispatcher.listener.PreProcessLitener;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.dispatcher.registration.CommandRegistrationContext;
import grapefruit.command.dispatcher.registration.CommandRegistrationHandler;
import grapefruit.command.dispatcher.registration.RedirectingCommandRegistration;
import grapefruit.command.dispatcher.registration.StandardCommandRegistration;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.MessageProvider;
import grapefruit.command.message.Messenger;
import grapefruit.command.message.Template;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMapperRegistry;
import grapefruit.command.parameter.mapper.ParameterMappingException;
import grapefruit.command.parameter.modifier.Source;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.BooleanFunction;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static grapefruit.command.dispatcher.CommandGraph.ALIAS_SEPARATOR;
import static java.lang.String.format;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

final class CommandDispatcherImpl<S> implements CommandDispatcher<S> {
    private static final System.Logger LOGGER = System.getLogger(CommandDispatcherImpl.class.getName());
    private final ParameterMapperRegistry<S> mapperRegistry = new ParameterMapperRegistry<>();
    private final MethodParameterParser<S> parameterParser = new MethodParameterParser<>(this.mapperRegistry);
    private final Queue<PreProcessLitener<S>> preProcessLiteners = new ConcurrentLinkedQueue<>();
    private final Queue<PreDispatchListener<S>> preDispatchListeners = new ConcurrentLinkedQueue<>();
    private final Queue<PostDispatchListener<S>> postDispatchListeners = new ConcurrentLinkedQueue<>();
    private final CommandInputTokenizer inputTokenizer = new CommandInputTokenizer();
    private final CommandGraph<S> commandGraph = new CommandGraph<>();
    private final SuggestionHelper<S> suggestionHelper = new SuggestionHelper<>();
    private final TypeToken<S> commandSourceType;
    private final CommandAuthorizer<S> commandAuthorizer;
    private final Executor directExecutor = Runnable::run;
    private final Executor asyncExecutor;
    private final MessageProvider messageProvider;
    private final CommandRegistrationHandler<S> registrationHandler;
    private final Messenger<S> messenger;

    CommandDispatcherImpl(final @NotNull TypeToken<S> commandSourceType,
                          final @NotNull CommandAuthorizer<S> commandAuthorizer,
                          final @NotNull Executor asyncExecutor,
                          final @NotNull MessageProvider messageProvider,
                          final @NotNull CommandRegistrationHandler<S> registrationHandler,
                          final @NotNull Messenger<S> messenger) {
        this.commandSourceType = requireNonNull(commandSourceType, "commandSourceType cannot be null");
        this.commandAuthorizer = requireNonNull(commandAuthorizer, "commandAuthorizer cannot be null");
        this.asyncExecutor = requireNonNull(asyncExecutor, "asyncExecutor cannot be null");
        this.messageProvider = requireNonNull(messageProvider, "messageProvider cannot be null");
        this.registrationHandler = requireNonNull(registrationHandler, "registrationHandler cannot be null");
        this.messenger = requireNonNull(messenger, "messenger cannot be null");
    }

    @Override
    public @NotNull ParameterMapperRegistry<S> mappers() {
        return this.mapperRegistry;
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

        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException(format("Static methods cannot be annotated with @CommandDefinition (%s)", method));
        }

        final String route = def.route();
        final Set<RedirectNode> redirectNodes = Arrays.stream(method.getAnnotationsByType(Redirect.class))
                .peek(x -> {
                    if (x.from().equalsIgnoreCase(route)) {
                        throw new IllegalArgumentException(format("Method %s has a @Redirect annotation that redirects to itself", method));
                    }
                })
                .map(x -> new RedirectNode(x.from(), x.arguments()))
                .collect(Collectors.toSet());
        final @Nullable String permission = Miscellaneous.emptyToNull(def.permission());
        final boolean runAsync = def.runAsync();

        try {
            method.setAccessible(true);
            final Parameter[] parameters = method.getParameters();
            final List<CommandParameter<S>> parsedParams = this.parameterParser.collectParameters(method);
            final @Nullable TypeToken<?> commandSourceType = (parameters.length > 0 && parameters[0].isAnnotationPresent(Source.class))
                    ? TypeToken.of(parameters[0].getAnnotatedType().getType())
                    : null;

            if (commandSourceType != null) {
                final Class<?> baseCommandSourceType = this.commandSourceType.getRawType();
                final Class<?> foundCommandSourceType = commandSourceType.getRawType();
                if (!baseCommandSourceType.isAssignableFrom(foundCommandSourceType)) {
                    throw new IllegalArgumentException(format("Invalid command source type detected (%s)! Must implement %s",
                            foundCommandSourceType.getName(), baseCommandSourceType.getName()));
                }
            }

            final CommandRegistration<S> reg = new StandardCommandRegistration<>(
                    container,
                    method,
                    parsedParams,
                    permission,
                    commandSourceType,
                    runAsync);
            registerCommand(route, reg);
            redirectNodes.forEach(node ->
                    registerCommand(node.route(), new RedirectingCommandRegistration<>(reg, Arrays.asList(node.arguments()))));
        } catch (final MethodParameterParser.RuleViolationException ex) {
            throw new RuntimeException(ex);
        } catch (final Throwable ex) {
            throw new RuntimeException(format("Failed to register command at '%s' (%s)", route, method), ex);
        }
    }

    private void registerCommand(final @NotNull String route,
                                 final @NotNull CommandRegistration<S> registration) {
        this.commandGraph.registerCommand(route, registration);
        final CommandRegistrationContext<S> regContext = new CommandRegistrationContext<>(
                Arrays.asList(route.split(" ")),
                registration
        );
        this.registrationHandler.accept(regContext);
    }

    private <L> boolean invokeListeners(final @NotNull Supplier<Queue<L>> listeners,
                                        final @NotNull BooleanFunction<L> action,
                                        final @NotNull BiConsumer<L, Throwable> errorHandler) {
        for (final L listener : listeners.get()) {
            try {
                if (!action.apply(listener)) {
                    return false;
                }
            } catch (final Throwable ex) {
                errorHandler.accept(listener, ex);
            }
        }

        return true;
    }

    private boolean invokePreProcessListeners(final @NotNull S source, final @NotNull String commandLine) {
        return invokeListeners(
                () -> this.preProcessLiteners,
                x -> x.onPreProcess(source, commandLine),
                (x, ex) -> {
                    LOGGER.log(WARNING, format("PreProcessListener %s threw an exception", x.getClass().getName()));
                    ex.printStackTrace();
                }
        );
    }

    private boolean invokePreDispatchListeners(final @NotNull CommandContext<S> context,
                                               final @NotNull CommandRegistration<S> registration) {
        return invokeListeners(
                () -> this.preDispatchListeners,
                x -> x.onPreDispatch(context, registration),
                (x, ex) -> {
                    LOGGER.log(WARNING, format("PreDispatchListener %s threw an exception", x.getClass().getName()));
                    ex.printStackTrace();
                }
        );
    }

    private void invokePostDispatchListeners(final @NotNull CommandContext<S> context) {
        invokeListeners(
                () -> this.postDispatchListeners,
                x -> {
                    x.onPostDispatch(context);
                    return true;
                },
                (x, ex) -> {
                    LOGGER.log(WARNING, format("PostDispatchListener %s threw an exception", x.getClass().getName()));
                    ex.printStackTrace();
                }
        );
    }

    @Override
    public void dispatchCommand(final @NotNull S source, final @NotNull String commandLine) {
        requireNonNull(source, "source cannot be null");
        requireNonNull(commandLine, "commandLine cannot be null");
        if (!invokePreProcessListeners(source, commandLine)) {
            return;
        }

        try {
            final Queue<CommandInput> args = this.inputTokenizer.tokenizeInput(commandLine);
            final CommandGraph.RouteResult<S> routeResult = this.commandGraph.routeCommand(args);
            if (routeResult instanceof CommandGraph.RouteResult.Success<S> success) {
                final CommandRegistration<S> reg = success.registration();
                if (reg instanceof RedirectingCommandRegistration<S> redirect) {
                    args.clear(); // Will use arguments provided by @Redirect#arguments
                    args.addAll(this.inputTokenizer.tokenizeInput(String.join(" ", redirect.rawArguments())));
                }

                final @Nullable String permission = reg.permission().orElse(null);
                if (!Miscellaneous.checkAuthorized(source, permission, this.commandAuthorizer)) {
                    throw new CommandAuthorizationException(permission);
                }

                final Optional<TypeToken<?>> requiredCommandSourceType = reg.commandSourceType();
                if (reg.commandSourceType().isPresent()) {
                    // Validate the type of the command source
                    final Class<?> foundCommandSourceClass = source.getClass();
                    final Class<?> requiredCommandSourceClass = requiredCommandSourceType.orElseThrow().getRawType();
                    if (!requiredCommandSourceClass.isAssignableFrom(foundCommandSourceClass)) {
                        throw new IllegalCommandSourceException(requiredCommandSourceClass, foundCommandSourceClass);
                    }
                }

                final Executor executor = reg.runAsync()
                        ? this.asyncExecutor
                        : this.directExecutor;
                executor.execute(() -> {
                    try {
                        final CommandContext<S> context = processCommand(reg, commandLine, source, args, false);
                        postprocessArguments(context, reg.parameters(), commandLine);
                        if (!invokePreDispatchListeners(context, reg)) {
                            return;
                        }

                        dispatchCommand(commandLine, reg, source, context.asMap().values());
                        invokePostDispatchListeners(context);
                    } catch (final CommandException ex) {
                        handleCommandException(source, ex);
                    }
                });
            } else {
                throw ((CommandGraph.RouteResult.Failure<S>) routeResult).reason().equals(
                        CommandGraph.RouteResult.Failure.Reason.NO_SUCH_COMMAND)
                            ? new NoSuchCommandException(commandLine.split(ALIAS_SEPARATOR)[0])
                            : new CommandSyntaxException(Message.of(
                            MessageKeys.TOO_FEW_ARGUMENTS,
                            Template.of("{syntax}", this.commandGraph.generateSyntaxFor(commandLine))
                ));
            }
        } catch (final CommandException ex) {
            handleCommandException(source, ex);
        }
    }

    private void dispatchCommand(final @NotNull String commandLine,
                                 final @NotNull CommandRegistration<S> reg,
                                 final @NotNull S source,
                                 final @NotNull Collection<Object> args) throws CommandException {
        try {
            final Object[] finalArgs;
            if (reg.commandSourceType().isPresent()) {
                finalArgs = new Object[args.size() + 1];
                finalArgs[0] = source;
                int idx = 1;
                for (final Object arg : args) {
                    finalArgs[idx++] = arg;
                }

            } else {
                finalArgs = args.toArray(Object[]::new);
            }

            reg.method().invoke(reg.holder(), finalArgs);
        } catch (final Throwable ex) {
            throw new CommandInvocationException(ex, commandLine);
        }
    }

    private @NotNull CommandContext<S> processCommand(final @NotNull CommandRegistration<S> registration,
                                                      final @NotNull String commandLine,
                                                      final @NotNull S source,
                                                      final Queue<CommandInput> args,
                                                      final boolean suggestions) throws CommandException {
        final List<CommandParameter<S>> parameters = registration.parameters();
        final CommandContext<S> context = CommandContext.create(source, commandLine, parameters);
        final SuggestionContext<S> suggestionContext = context.suggestions();
        try {
            int parameterIndex = 0;
            CommandInput input;
            while ((input = args.peek()) != null) {
                suggestionContext.reset();
                suggestionContext.input(input);

                try {
                    final String rawInput = input.rawArg();
                    final Matcher matcher = FlagGroup.VALID_PATTERN.matcher(rawInput);
                    if (matcher.matches()) {
                        final FlagGroup<S> flags = FlagGroup.parse(rawInput, matcher, parameters);
                        for (final FlagParameter<S> flag : flags) {
                            consumeFlag(flag, context, args, input, rawInput);
                        }

                    } else {
                        consumeArgument(commandLine, parameters, context, args, parameterIndex);
                    }

                    parameterIndex++;
                    if (!args.isEmpty()) {
                        args.element().markConsumed();
                    }
                } finally {
                    if (!args.isEmpty() && args.element().isConsumed()) {
                        args.remove();
                    }
                }

            }

            return context;
        } catch (final Throwable ex) {
            if (suggestions) {
                return context;
            }

            if (ex instanceof CommandException) {
                throw (CommandException) ex;
            }

            throw new CommandInvocationException(ex, commandLine);
        }
    }

    private void consumeFlag(final @NotNull FlagParameter<S> flag,
                             final @NotNull CommandContext<S> context,
                             final @NotNull Queue<CommandInput> args,
                             final @NotNull CommandInput input,
                             final @NotNull String rawInput) throws CommandException {
        input.markConsumed();
        final String flagName = flag.flagName();
        final Optional<Object> stored = context.find(flagName);
        if (stored.isPresent()) {
            throw new FlagDuplicateException(flagName);
        }

        final SuggestionContext<S> suggestionContext = context.suggestions();
        suggestionContext.parameter(flag);
        suggestionContext.flagNameConsumed(true);

        if (flag.type().equals(FlagParameter.PRESENCE_FLAG_TYPE)) {
            context.put(flagName, true);
        } else {
            args.remove();
            if (args.isEmpty()) {
                // This means that there aren't any values for this flag
                throw new CommandSyntaxException(Message.of(
                        MessageKeys.MISSING_FLAG_VALUE,
                        Template.of("{input}", rawInput)
                ));
            }

            final Object parsedValue = mapParameter(flag, context, args);
            context.put(flagName, parsedValue);
            suggestionContext.input(args.element());
        }
    }

    private void consumeArgument(final @NotNull String commandLine,
                                 final @NotNull List<CommandParameter<S>> parameters,
                                 final @NotNull CommandContext<S> context,
                                 final @NotNull Queue<CommandInput> args,
                                 final int parameterIndex) throws CommandException {
        if (parameterIndex >= parameters.size()) {
            throw new CommandSyntaxException(Message.of(
                    MessageKeys.TOO_MANY_ARGUMENTS,
                    Template.of("{syntax}", this.commandGraph.generateSyntaxFor(commandLine))
            ));
        }

        final SuggestionContext<S> suggestionContext = context.suggestions();
        final Optional<CommandParameter<S>> firstNonFlagParameter = parameters.stream()
                .filter(x -> !x.isFlag())
                .filter(x -> context.find(x.name()).isEmpty())
                .findFirst();
        if (firstNonFlagParameter.isEmpty()) {
            suggestionContext.parameter(null);
            throw new CommandSyntaxException(Message.of(MessageKeys.MISSING_FLAG,
                    Template.of("{syntax}", this.commandGraph.generateSyntaxFor(commandLine))));
        }

        final CommandParameter<S> parameter = firstNonFlagParameter.orElseThrow();
        suggestionContext.parameter(parameter);
        final Object parsedValue = mapParameter(parameter, context, args);
        context.put(parameter.name(), parsedValue);
    }

    private @Nullable Object mapParameter(final @NotNull CommandParameter<S> parameter,
                                          final @NotNull CommandContext<S> context,
                                          final @NotNull Queue<CommandInput> args) throws ParameterMappingException {
        final ParameterMapper<S, ?> mapper = parameter.mapper();
        final AnnotationList modifiers = parameter.modifiers();
        try {
            return mapper.map(context, args, modifiers);
        } catch (final ParameterMappingException ex) {
            if (parameter.isOptional() && !parameter.isFlag()) {
                return null;
            }

            throw ex;
        }
    }

    private void postprocessArguments(final @NotNull CommandContext<S> context,
                                      final @NotNull List<CommandParameter<S>> parameters,
                                      final @NotNull String commandLine) throws CommandException {
        for (final CommandParameter<S> parameter : parameters) {
            final String name = Miscellaneous.parameterName(parameter);
            final Optional<Object> argument = context.find(name);
            if (!parameter.isOptional() && argument.isEmpty()) {
                throw new CommandSyntaxException(Message.of(
                        MessageKeys.TOO_FEW_ARGUMENTS,
                        Template.of("{syntax}", this.commandGraph.generateSyntaxFor(commandLine))
                ));
            }
        }
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String commandLine) {
        requireNonNull(source, "source cannot be null");
        final Deque<CommandInput> args = new ArrayDeque<>(this.inputTokenizer.tokenizeInput(commandLine));
        if (args.size() == 0) {
            return List.of();
        }

        final boolean suggestNext = commandLine.charAt(commandLine.length() - 1) == ' ';
        final String last = suggestNext ? "" : args.getLast().rawArg().trim();
        /*
         * Both routeCommand and processCommand remove elements from the queue
         * but we need all elements for proper auto-completion
         */
        final Queue<CommandInput> argsCopy = new ArrayDeque<>(args);
        final CommandGraph.RouteResult<S> routeResult = this.commandGraph.routeCommand(args);
        final List<String> suggestions = new ArrayList<>();

        if (routeResult instanceof CommandGraph.RouteResult.Success<S> success) {
            final CommandRegistration<S> registration = success.registration();
            if (!Miscellaneous.checkAuthorized(source, registration.permission().orElse(null), this.commandAuthorizer)) {
                return List.of();
            }

            try {
                final CommandContext<S> context = processCommand(registration, commandLine, source, args, true);
                if (suggestNext) {
                    context.suggestions().suggestNext(true);
                }

                suggestions.addAll(this.suggestionHelper.listSuggestions(context, registration, args));
            } catch (final CommandException ignored) {}
        } else {
            suggestions.addAll(this.commandGraph.listSuggestions(argsCopy));
        }

        return suggestions.stream()
                .filter(x -> Miscellaneous.startsWithIgnoreCase(x, last))
                .toList();
    }

    private void handleCommandException(final @NotNull S source, final @NotNull CommandException ex) {
        final Message message = ex.message();
        this.messenger.sendMessage(source, message.get(this.messageProvider));
        /*
         * CommandInvocationException means that things went south
         */
        if (ex instanceof CommandInvocationException) {
            ex.printStackTrace();
        }
    }
}
