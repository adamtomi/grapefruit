package grapefruit.command.dispatcher;

import grapefruit.command.message.DefaultMessageProvider;
import grapefruit.command.message.MessageProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;

public abstract class CommandDispatcherBuilder<S, D extends CommandDispatcher<S>, B extends CommandDispatcherBuilder<S, D, B>> {
    private static final System.Logger LOGGER = System.getLogger(CommandDispatcherBuilder.class.getName());
    private CommandAuthorizer<S> authorizer;
    private Executor asyncExecutor;
    private MessageProvider messageProvider;

    protected abstract @NotNull B self();

    protected abstract D build(final @NotNull CommandAuthorizer<S> authorizer,
                               final @NotNull Executor asyncExecutor,
                               final @NotNull MessageProvider messageProvider);

    public @NotNull B withAuthorizer(final @NotNull CommandAuthorizer<S> authorizer) {
        this.authorizer = requireNonNull(authorizer, "authorizer cannot be null");
        return self();
    }

    public @NotNull B withAsyncExecutor(final @NotNull Executor asyncExecutor) {
        this.asyncExecutor = requireNonNull(asyncExecutor, "asyncExecutor cannot be null");
        return self();
    }

    public @NotNull B withMessageProvider(final @NotNull MessageProvider messageProvider) {
        this.messageProvider = requireNonNull(messageProvider, "messageProvider cannot be null");
        return self();
    }

    @SuppressWarnings("unchecked")
    public @NotNull D validateAndBuild() {
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
            LOGGER.log(WARNING, "No MessageProvider specified, defaulting to %s", DefaultMessageProvider.class);
            messageProvider = new DefaultMessageProvider();
        } else {
            messageProvider = this.messageProvider;
        }

        return build(authorizer, asyncExecutor, messageProvider);
    }
}
