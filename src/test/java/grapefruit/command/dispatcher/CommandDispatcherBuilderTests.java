package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.registration.CommandRegistrationContext;
import grapefruit.command.dispatcher.registration.CommandRegistrationHandler;
import grapefruit.command.message.DefaultMessageProvider;
import grapefruit.command.message.MessageKey;
import grapefruit.command.message.MessageProvider;
import grapefruit.command.message.Messenger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommandDispatcherBuilderTests {
    private Field authorizerField;
    private Field asyncExecutorField;
    private Field messengerField;
    private Field registrationHandlerField;
    private Field messageProviderField;

    @BeforeAll
    @SuppressWarnings("rawtypes")
    public void setUp() throws ReflectiveOperationException {
        final Class<CommandDispatcherImpl> clazz = CommandDispatcherImpl.class;
        this.authorizerField = accessField(clazz, "commandAuthorizer");
        this.asyncExecutorField = accessField(clazz, "asyncExecutor");
        this.messageProviderField = accessField(clazz, "messageProvider");
        this.messengerField = accessField(clazz, "messenger");
        this.registrationHandlerField = accessField(clazz, "registrationHandler");
    }

    private Field accessField(final Class<?> holder, final String name) throws ReflectiveOperationException {
        final Field field = holder.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static CommandDispatcherBuilder<Object> builder() {
        return CommandDispatcher.builder(TypeToken.of(Object.class));
    }

    @Test
    public void build_defaultAuthorizer() throws ReflectiveOperationException {
        final CommandDispatcher<?> dispatcher = builder().build();
        final CommandAuthorizer<?> authorizer = (CommandAuthorizer<?>) this.authorizerField.get(dispatcher);
        assertEquals(CommandAuthorizer.NO_OP, authorizer);
    }

    @Test
    public void build_customAuthorizer() throws ReflectiveOperationException {
        final CommandAuthorizer<Object> authorizer = new DummyCommandAuthorizer();
        final CommandDispatcher<?> dispatcher = builder().withAuthorizer(authorizer).build();
        final CommandAuthorizer<?> actual = (CommandAuthorizer<?>) this.authorizerField.get(dispatcher);
        assertEquals(authorizer, actual);
    }

    @Test
    public void build_defaultExecutor() throws ReflectiveOperationException {
        final CommandDispatcher<?> dispatcher = builder().build();
        final Executor asyncExecutor = (Executor) this.asyncExecutorField.get(dispatcher);
        assertEquals(ForkJoinPool.commonPool(), asyncExecutor);
    }

    @Test
    public void build_customExecutor() throws ReflectiveOperationException {
        final Executor asyncExecutor = Executors.newCachedThreadPool();
        final CommandDispatcher<?> dispatcher = builder().withAsyncExecutor(asyncExecutor).build();
        final Executor actual = (Executor) this.asyncExecutorField.get(dispatcher);
        assertEquals(asyncExecutor, actual);
    }

    @Test
    public void build_defaultMessageProvider() throws ReflectiveOperationException {
        final CommandDispatcher<?> dispatcher = builder().build();
        final MessageProvider provider = (MessageProvider) this.messageProviderField.get(dispatcher);
        assertEquals(DefaultMessageProvider.class, provider.getClass());
    }

    @Test
    public void build_customMessageProvider() throws ReflectiveOperationException {
        final MessageProvider provider = new DummyMessageProvider();
        final CommandDispatcher<?> dispatcher = builder().withMessageProvider(provider).build();
        final MessageProvider actual = (MessageProvider) this.messageProviderField.get(dispatcher);
        assertEquals(provider, actual);
    }

    @Test
    public void build_defaultRegistrationHandler() throws ReflectiveOperationException {
        final CommandDispatcher<?> dispatcher = builder().build();
        final CommandRegistrationHandler<?> handler = (CommandRegistrationHandler<?>) this.registrationHandlerField.get(dispatcher);
        assertEquals(CommandRegistrationHandler.NO_OP, handler);
    }

    @Test
    public void build_customRegistrationHandler() throws ReflectiveOperationException {
        final CommandRegistrationHandler<Object> handler = new DummyCommandRegistrationHandler();
        final CommandDispatcher<?> dispatcher = builder().withRegistrationHandler(handler).build();
        final CommandRegistrationHandler<?> actual = (CommandRegistrationHandler<?>) this.registrationHandlerField.get(dispatcher);
        assertEquals(handler, actual);
    }

    @Test
    public void build_defaultMessenger() throws ReflectiveOperationException {
        final CommandDispatcher<?> dispatcher = builder().build();
        final Messenger<?> messenger = (Messenger<?>) this.messengerField.get(dispatcher);
        assertEquals(Messenger.builtin(), messenger);
    }

    @Test
    public void build_customMessenger() throws ReflectiveOperationException {
        final Messenger<Object> messenger = new DummyMessenger();
        final CommandDispatcher<?> dispatcher = builder().withMessenger(messenger).build();
        final Messenger<?> actual = (Messenger<?>) this.messengerField.get(dispatcher);
        assertEquals(messenger, actual);
    }

    private static final class DummyCommandAuthorizer implements CommandAuthorizer<Object> {
        @Override
        public boolean isAuthorized(final @NotNull Object source, final @NotNull String permission) {
            return false;
        }
    }

    private static final class DummyMessageProvider implements MessageProvider {
        @Override
        public @NotNull String provide(final @NotNull MessageKey key) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class DummyCommandRegistrationHandler implements CommandRegistrationHandler<Object> {
        @Override
        public void accept(final @NotNull CommandRegistrationContext<Object> context) {}
    }

    private static final class DummyMessenger implements Messenger<Object> {
        @Override
        public void sendMessage(final @NotNull Object source, final @NotNull String message) {}
    }
}
