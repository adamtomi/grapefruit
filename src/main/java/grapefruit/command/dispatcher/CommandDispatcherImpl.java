package grapefruit.command.dispatcher;

import grapefruit.command.CommandContainer;
import grapefruit.command.CommandDefinition;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.CommandAuthorizationException;
import grapefruit.command.dispatcher.exception.CommandInvocationException;
import grapefruit.command.dispatcher.exception.CommandSyntaxException;
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
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static grapefruit.command.dispatcher.CommandGraph.ALIAS_SEPARATOR;
import static grapefruit.command.parameter.FlagParameter.FLAG_PATTERN;
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
    private final TypeToken<S> commandSourceType;
    private final CommandAuthorizer<S> commandAuthorizer;
    private final CommandGraph<S> commandGraph;
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
        this.commandGraph = new CommandGraph<>(this.commandAuthorizer);
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

        final String route = def.route();
        final @Nullable String permission = Miscellaneous.emptyToNull(def.permission());
        final boolean runAsync = def.runAsync();

        try {
            final Parameter[] parameters = method.getParameters();
            final List<CommandParameter<S>> parsedParams = this.parameterParser.collectParameters(method);
            final @Nullable TypeToken<?> commandSourceType = (parameters.length > 0 && parameters[0].isAnnotationPresent(Source.class))
                    ? TypeToken.get(parameters[0].getType())
                    : null;

            if (commandSourceType != null) {
                final Class<?> baseCommandSourceType = GenericTypeReflector.erase(this.commandSourceType.getType());
                final Class<?> foundCommandSourceType = GenericTypeReflector.erase(commandSourceType.getType());
                if (!baseCommandSourceType.isAssignableFrom(foundCommandSourceType)) {
                    throw new IllegalArgumentException(format("Invalid command source type detected (%s)! Must implement %s",
                            foundCommandSourceType.getName(), baseCommandSourceType.getName()));
                }
            }

            final CommandRegistration<S> reg = new CommandRegistration<>(
                    container,
                    method,
                    parsedParams,
                    permission,
                    commandSourceType,
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

    private boolean invokePreDispatchListeners(final @NotNull S source,
                                               final @NotNull String commandLine,
                                               final @NotNull CommandRegistration<S> registration) {
        return invokeListeners(
                () -> this.preDispatchListeners,
                x -> x.onPreDispatch(source, commandLine, registration),
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
                final @Nullable String permission = reg.permission();

                if (!Miscellaneous.checkAuthorized(source, permission, this.commandAuthorizer)) {
                    throw new CommandAuthorizationException(permission);
                }

                if (reg.commandSourceType() != null) {
                    // Validate the type of the command source
                    final Class<?> foundCommandSourceType = source.getClass();
                    final Class<?> requiredCommandSourceType = GenericTypeReflector.erase(reg.commandSourceType().getType());
                    if (!requiredCommandSourceType.isAssignableFrom(foundCommandSourceType)) {
                        throw new IllegalCommandSourceException(requiredCommandSourceType, foundCommandSourceType);
                    }
                }

                if (!invokePreDispatchListeners(source, commandLine, reg)) {
                    return;
                }

                final Executor executor = reg.runAsync()
                        ? this.asyncExecutor
                        : this.directExecutor;
                executor.execute(() -> {
                    try {
                        final CommandContext<S> commandContext = dispatchCommand(reg, commandLine, source, args);
                        invokePostDispatchListeners(commandContext);
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

    private @NotNull CommandContext<S> dispatchCommand(final @NotNull CommandRegistration<S> registration,
                                                       final @NotNull String commandLine,
                                                       final @NotNull S source,
                                                       final @NotNull Queue<CommandInput> args) throws CommandException {
        final CommandContext<S> context = new CommandContext<>(source, commandLine, preprocessArguments(registration.parameters()));
        try {
            int parameterIndex = 0;
            CommandInput input;
            while ((input = args.peek()) != null) {
                try {
                    final String rawInput = input.rawArg();
                    final Matcher matcher = FLAG_PATTERN.matcher(rawInput);
                    if (matcher.matches()) {
                        final String flagName = matcher.group(1).toLowerCase(Locale.ROOT);
                        final Optional<CommandParameter<S>> flagParameterOpt = registration.parameters()
                                .stream()
                                .filter(CommandParameter::isFlag)
                                .filter(param -> ((FlagParameter<S>) param).flagName().equals(flagName))
                                .findFirst();

                        if (flagParameterOpt.isEmpty()) {
                            throw new CommandException(Message.of(
                                    MessageKeys.UNRECOGNIZED_COMMAND_FLAG,
                                    Template.of("{input}", rawInput)
                            ));
                        }

                        input.markConsumed();
                        args.remove();
                        final CommandParameter<S> flagParameter = flagParameterOpt.get();
                        final ParsedCommandArgument parsedArg = context.findArgumentUnchecked(flagName);
                        if (flagParameter.type().equals(FlagParameter.PRESENCE_FLAG_TYPE)) {
                            parsedArg.parsedValue(true);

                        } else {
                            if (args.isEmpty()) {
                                // This means that there aren't any values for this flag
                                throw new CommandSyntaxException(Message.of(
                                        MessageKeys.MISSING_FLAG_VALUE,
                                        Template.of("{input}", rawInput)
                                ));
                            }

                            final Object parsedValue = mapParameter(flagParameter, context, args);
                            parsedArg.parsedValue(parsedValue);
                        }

                    } else {
                        final List<CommandParameter<S>> parameters = registration.parameters();
                        if (parameterIndex >= parameters.size()) {
                            throw new CommandSyntaxException(Message.of(
                                    MessageKeys.TOO_MANY_ARGUMENTS,
                                    Template.of("{syntax}", this.commandGraph.generateSyntaxFor(commandLine))
                            ));
                        }

                        int actualIndex = parameterIndex;
                        CommandParameter<S> parameter = parameters.get(parameterIndex);
                        if (parameter.isFlag()) {
                            final Optional<CommandParameter<S>> firstNonFlagParameter = parameters.stream()
                                    .filter(x -> !x.isFlag())
                                    .filter(x -> context.findArgument(x.name()).isEmpty())
                                    .findFirst();
                            if (firstNonFlagParameter.isEmpty()) {
                                throw new CommandSyntaxException(Message.of(MessageKeys.MISSING_FLAG,
                                        Template.of("{syntax}", this.commandGraph.generateSyntaxFor(commandLine))));
                            }

                            parameter = firstNonFlagParameter.orElseThrow();
                            actualIndex = parameters.indexOf(parameter);
                        }

                        final Object parsedValue = mapParameter(parameter, context, args);
                        final ParsedCommandArgument parsedArg = context.findArgumentAtUnsafe(actualIndex);
                        parsedArg.parsedValue(parsedValue);
                        parameterIndex++;
                    }

                    if (!args.isEmpty()) {
                        args.element().markConsumed();
                    }
                } finally {
                    if (!args.isEmpty() && args.element().isConsumed()) {
                        args.remove();
                    }
                }
            }

            dispatchCommand0(registration, source, postprocessArguments(context, registration.parameters(), commandLine));
            return context;
        } catch (final Throwable ex) {
            if (ex instanceof CommandException) {
                throw (CommandException) ex;
            }

            throw new CommandInvocationException(ex, commandLine);
        }
    }

    private @Nullable Object mapParameter(final @NotNull CommandParameter<S> parameter,
                                          final @NotNull CommandContext<S> context,
                                          final @NotNull Queue<CommandInput> args) throws ParameterMappingException {
        final ParameterMapper<S, ?> mapper = parameter.mapper();
        final AnnotationList modifiers = parameter.modifiers();
        try {
            return mapper.map(context, args, modifiers);
        } catch (final ParameterMappingException ex) {
            if (parameter.isOptional()) {
                return null;
            }

            throw ex;
        }
    }

    private @NotNull List<ParsedCommandArgument> preprocessArguments(final @NotNull Collection<CommandParameter<S>> parameters) {
        return parameters.stream()
                .map(parameter -> {
                    final String name = parameter.isFlag()
                            ? ((FlagParameter<S>) parameter).flagName()
                            : parameter.name();
                    final ParsedCommandArgument parsedArg = new ParsedCommandArgument(name);

                    if (parameter.isOptional()) {
                        final @NotNull Class<?> type = GenericTypeReflector.erase(parameter.type().getType());
                        final Object defaultValue = type.isPrimitive()
                                ? Miscellaneous.nullToPrimitive(type)
                                : null;
                        parsedArg.parsedValue(defaultValue);
                    }

                    return parsedArg;
                })
                .collect(Collectors.toList());
    }

    private @NotNull List<Object> postprocessArguments(final @NotNull CommandContext<S> result,
                                                       final @NotNull List<CommandParameter<S>> parameters,
                                                       final @NotNull String commandLine) throws CommandException {
        final List<Object> objects = new ArrayList<>();
        for (final CommandParameter<S> param : parameters) {
            final String parameterName = param.isFlag()
                    ? ((FlagParameter<S>) param).flagName()
                    : param.name();
            final ParsedCommandArgument parsedArg = result.findArgumentUnchecked(parameterName);
            if (!param.isOptional() && parsedArg.parsedValue().isEmpty()) {
                throw new CommandSyntaxException(Message.of(
                        MessageKeys.TOO_FEW_ARGUMENTS,
                        Template.of("{syntax}", this.commandGraph.generateSyntaxFor(commandLine))
                ));
            }

            objects.add(parsedArg.parsedValue().orElse(null));
        }

        return objects;
    }

    private void dispatchCommand0(final @NotNull CommandRegistration<S> reg,
                                  final @NotNull S source,
                                  final @NotNull Collection<Object> args) throws Throwable {
        final Object[] finalArgs;
        if (reg.commandSourceType() != null) {
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
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String commandLine) {
        requireNonNull(source, "source cannot be null");
        final Deque<CommandInput> args = Arrays.stream(commandLine.split(" "))
                .map(String::trim)
                .map(StringCommandInput::new)
                .collect(Collectors.toCollection(ArrayDeque::new));
        if (args.size() == 0) {
            return List.of();
        }

        if (commandLine.charAt(commandLine.length() - 1) == ' ') {
            args.add(new BlankCommandInput(1)); // This is a bit hacky
        }

        final String last = args.getLast().rawArg();
        final CommandGraph.RouteResult<S> routeResult = this.commandGraph.routeCommand(args);
        CommandContext<S> context = new CommandContext<>(source, commandLine, List.of());

        if (routeResult instanceof CommandGraph.RouteResult.Success<S> success) {
            final CommandRegistration<S> registration = success.registration();
            try {
                context = dispatchCommand(registration, commandLine, source, args);
            } catch (final CommandException ignored) {}

        }

        return this.commandGraph.listSuggestions(context, args)
                .stream()
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
