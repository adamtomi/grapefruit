package grapefruit.command.dispatcher;

import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.argument.DuplicateFlagException;
import grapefruit.command.argument.UnrecognizedFlagException;
import grapefruit.command.argument.condition.UnfulfilledConditionException;
import grapefruit.command.dispatcher.config.DispatcherConfig;
import grapefruit.command.mock.TestArgumentMapper;
import grapefruit.command.mock.TestCommandModule;
import grapefruit.command.tree.NoSuchCommandException;
import grapefruit.command.util.key.Key;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.word;
import static grapefruit.command.mock.AlwaysCondition.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandDispatcherTests {

    @Test
    public void register_duplicateCommand() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        assertDoesNotThrow(() -> dispatcher.register(command));
        assertThrows(IllegalStateException.class, () -> dispatcher.register(command));
    }

    @Test
    public void register_registrationInterrupted() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .register(x -> false) // Always deny registration
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        assertDoesNotThrow(() -> dispatcher.register(command));
        assertThrows(NoSuchCommandException.class, () -> dispatcher.dispatch(new Object(), "test"));
    }

    @Test
    public void register_success() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        assertDoesNotThrow(() -> dispatcher.register(command));
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
    }

    @Test
    public void unregister_unrecognizedCommand() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        assertThrows(IllegalStateException.class, () -> dispatcher.unregister(command));
    }

    @Test
    public void unregister_unregistrationInterrupted() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .unregister(x -> false) // Always deny unregistration
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        dispatcher.register(command);
        dispatcher.unregister(command);

        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
    }

    @Test
    public void unregister_success() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        dispatcher.register(command);
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));

        dispatcher.unregister(command);
        assertThrows(NoSuchCommandException.class, () -> dispatcher.dispatch(new Object(), "test"));
    }

    @Test
    public void dispatch_contextDecoratorCalled() {
        final AtomicBoolean state = new AtomicBoolean(false);
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .decorateContext((context, mode) -> state.set(true))
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        dispatcher.register(command);
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
        assertTrue(state.get());
    }

    @Test
    public void dispatch_conditionFailed() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").expect(fail()).build()).build());

        dispatcher.register(command);
        assertThrows(UnfulfilledConditionException.class, () -> dispatcher.dispatch(new Object(), "test"));
    }

    @Test
    public void dispatch_notEnoughArguments() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .arguments()
                .then(factory.required(Key.named(String.class, "arg")).mapWith(word()).build())
                .build());

        dispatcher.register(command);
        assertThrows(CommandSyntaxException.class, () -> dispatcher.dispatch(new Object(), "test"));
    }

    @Test
    public void dispatch_tooManyArguments() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .arguments()
                .then(factory.required(Key.named(String.class, "arg")).mapWith(word()).build())
                .build());

        dispatcher.register(command);
        assertThrows(CommandSyntaxException.class, () -> dispatcher.dispatch(new Object(), "test arg hello"));
    }

    @Test
    public void dispatch_invalidArguments() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .arguments()
                .then(factory.required(Key.named(String.class, "arg")).mapWith(new TestArgumentMapper("hello")).build())
                .build());

        dispatcher.register(command);
        assertThrows(CommandArgumentException.class, () -> dispatcher.dispatch(new Object(), "test arg"));
    }

    @Test
    public void dispatch_flagIsOptional() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.presenceFlag("hello").build())
                .build());

        dispatcher.register(command);

        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test --hello"));
    }

    @Test
    public void dispatch_unrecognizedFlag() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.presenceFlag("hello").build())
                .build());

        dispatcher.register(command);

        assertThrows(UnrecognizedFlagException.class, () -> dispatcher.dispatch(new Object(), "test --flag"));
    }

    @Test
    public void dispatch_duplicateFlag() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.presenceFlag("hello").build())
                .build());

        dispatcher.register(command);

        assertThrows(DuplicateFlagException.class, () -> dispatcher.dispatch(new Object(), "test --hello --hello"));
    }

    @Test
    public void dispatch_flagConditionFailed() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.presenceFlag("hello").expect(fail()).build())
                .build());

        dispatcher.register(command);

        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
        assertThrows(UnfulfilledConditionException.class, () -> dispatcher.dispatch(new Object(), "test --hello"));
    }

    @Test
    public void dispatch_flagShorthands_success() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.presenceFlag("hello").assumeShorthand().build())
                .build());

        dispatcher.register(command);

        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test --hello"));
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test -h"));
    }

    @Test
    public void dispatch_flagShorthands_failure() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.presenceFlag("hello").build())
                .build());

        dispatcher.register(command);

        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test --hello"));
        assertThrows(UnrecognizedFlagException.class, () -> dispatcher.dispatch(new Object(), "test -h"));
    }

    public void dispatch_flagGroup() {

    }

    public void dispatch_complexExamples() {

    }
}
