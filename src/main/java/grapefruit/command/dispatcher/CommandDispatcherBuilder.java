package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.registration.CommandRegistrationHandler;
import grapefruit.command.message.DefaultMessageProvider;
import grapefruit.command.message.MessageProvider;
import grapefruit.command.message.Messenger;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

public class CommandDispatcherBuilder<S> {
    private static final System.Logger LOGGER = System.getLogger(CommandDispatcherBuilder.class.getName());
    private final TypeToken<S> commandSourceType;
    private CommandAuthorizer<S> authorizer;
    private Executor asyncExecutor;
    private MessageProvider messageProvider;
    private CommandRegistrationHandler<S> registrationHandler;
    private Messenger<S> messenger;

    protected CommandDispatcherBuilder(final @NotNull TypeToken<S> commandSourceType) {
        this.commandSourceType = requireNonNull(commandSourceType, "commandSourceType cannot be null");
    }

    public @NotNull CommandDispatcherBuilder<S> withAuthorizer(final @NotNull CommandAuthorizer<S> authorizer) {
        this.authorizer = requireNonNull(authorizer, "authorizer cannot be null");
        return this;
    }

    public @NotNull CommandDispatcherBuilder<S> withAsyncExecutor(final @NotNull Executor asyncExecutor) {
        this.asyncExecutor = requireNonNull(asyncExecutor, "asyncExecutor cannot be null");
        return this;
    }

    public @NotNull CommandDispatcherBuilder<S> withMessageProvider(final @NotNull MessageProvider messageProvider) {
        this.messageProvider = requireNonNull(messageProvider, "messageProvider cannot be null");
        return this;
    }

    public @NotNull CommandDispatcherBuilder<S> withRegistrationHandler(final @NotNull CommandRegistrationHandler<S> registrationHandler) {
        this.registrationHandler = requireNonNull(registrationHandler, "registrationHandler cannot be null");
        return this;
    }

    public @NotNull CommandDispatcherBuilder<S> withMessenger(final @NotNull Messenger<S> messenger) {
        this.messenger = requireNonNull(messenger, "messenger cannot be null");
        return this;
    }

    @SuppressWarnings("unchecked")
    public @NotNull CommandDispatcher<S> validateAndBuild() {
        final CommandAuthorizer<S> authorizer;
        if (this.authorizer == null) {
            LOGGER.log(WARNING, "No CommandAuthorizer specified, defaulting to no-op implementation");
            authorizer = (CommandAuthorizer<S>) CommandAuthorizer.NO_OP;
        } else {
            authorizer = this.authorizer;
        }

        final Executor asyncExecutor;
        if (this.asyncExecutor == null) {
            LOGGER.log(WARNING, "No async executor specified, defaulting to Executors#newCachedThreadPool");
            asyncExecutor = Executors.newCachedThreadPool();
        } else {
            asyncExecutor = this.asyncExecutor;
        }

        final MessageProvider messageProvider;
        if (this.messageProvider == null) {
            LOGGER.log(WARNING, "No MessageProvider specified, defaulting to %s", DefaultMessageProvider.class.getName());
            messageProvider = new DefaultMessageProvider();
        } else {
            messageProvider = this.messageProvider;
        }

        final CommandRegistrationHandler<S> registrationHandler;
        if (this.registrationHandler == null) {
            LOGGER.log(WARNING, "No CommandRegistrationHandler specified, defaulting to no-op implementation");
            registrationHandler = (CommandRegistrationHandler<S>) CommandRegistrationHandler.NO_OP;
        } else {
            registrationHandler = this.registrationHandler;
        }

        final Messenger<S> messenger;
        if (this.messenger == null) {
            messenger = Messenger.builtin();
            LOGGER.log(WARNING, "No Messenger specified, defaulting to %s", messenger.getClass().getName());
        } else {
            messenger = this.messenger;
        }

        return new CommandDispatcherImpl<>(
                this.commandSourceType,
                authorizer,
                asyncExecutor,
                messageProvider,
                registrationHandler,
                messenger
        );
    }
}
