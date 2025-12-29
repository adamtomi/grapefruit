package grapefruit.command.dispatcher;

import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandArgumentException;
import grapefruit.command.argument.DuplicateFlagException;
import grapefruit.command.argument.FlagGroupException;
import grapefruit.command.argument.UnrecognizedFlagException;
import grapefruit.command.argument.condition.UnfulfilledConditionException;
import grapefruit.command.completion.CommandCompletion;
import grapefruit.command.dispatcher.config.DispatcherConfig;
import grapefruit.command.mock.ColorArgumentMapper;
import grapefruit.command.mock.TestArgumentMapper;
import grapefruit.command.mock.TestCommandModule;
import grapefruit.command.tree.NoSuchCommandException;
import grapefruit.command.util.key.Key;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.word;
import static grapefruit.command.mock.AlwaysCondition.fail;
import static grapefruit.command.testutil.ExtraAssertions.assertContainsAll;
import static grapefruit.command.testutil.Helper.completions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
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
    public void unregister_multipleChildren() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command1 = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("foo").build())
                .build());

        final CommandModule<Object> command2 = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("bar").build())
                .build());

        dispatcher.register(command1);
        dispatcher.register(command2);

        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test foo"));
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test bar"));

        assertDoesNotThrow(() -> dispatcher.unregister(command1));

        assertThrows(NoSuchCommandException.class, () -> dispatcher.dispatch(new Object(), "test foo"));

        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test bar"));

        assertDoesNotThrow(() -> dispatcher.unregister(command2));

        assertThrows(NoSuchCommandException.class, () -> dispatcher.dispatch(new Object(), "test bar"));
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
                .then(factory.boolFlag("hello").build())
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
                .then(factory.boolFlag("hello").build())
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
                .then(factory.boolFlag("hello").build())
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
                .then(factory.boolFlag("hello").expect(fail()).build())
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
                .then(factory.boolFlag("hello").assumeShorthand().build())
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
                .then(factory.boolFlag("hello").build())
                .build());

        dispatcher.register(command);

        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test --hello"));
        assertThrows(UnrecognizedFlagException.class, () -> dispatcher.dispatch(new Object(), "test -h"));
    }

    @Test
    public void dispatch_flagGroup() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.boolFlag("hello").assumeShorthand().build())
                .then(factory.valueFlag("color", String.class).assumeShorthand().mapWith(new ColorArgumentMapper()).build())
                .build());

        dispatcher.register(command);
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test"));
        assertDoesNotThrow(() -> dispatcher.dispatch(new Object(), "test --hello --color #ffffff"));
        assertThrows(FlagGroupException.class, () -> dispatcher.dispatch(new Object(), "test -hc #ffffff"));
    }

    @Test
    public void dispatch_tooMany_unrecognizedFlag() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.boolFlag("hello").assumeShorthand().build())
                .build());

        dispatcher.register(command);
        assertThrows(UnrecognizedFlagException.class, () -> dispatcher.dispatch(new Object(), "test abc"));
    }

    @Test
    public void dispatch_tooMany_syntaxError() {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.boolFlag("hello").assumeShorthand().build())
                .build());

        dispatcher.register(command);
        assertThrows(CommandSyntaxException.class, () -> dispatcher.dispatch(new Object(), "test --hello abc"));
    }

    @ParameterizedTest
    @CsvSource({
            "'',command|cmd|test,''",
            "t,test,t",
            "c,command|cmd,c",
            "'test he ','',' '",
            "'test ',hello|hl,''",
            "'cmd su',sub|subcmd,su",
            "'cmd sub',sub|subcmd,sub",
            "test asd he,'',he",
            "'cmd su ','',' '"
    })
    public void complete_commandNames(final String input, final String expected, final String lastInput) {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command0 = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("hello").aliases("hl").build())
                .build());

        final CommandModule<Object> command1 = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("command").aliases("cmd").build())
                .then(factory.literal("sub").aliases("sb", "subcmd", "sc").build())
                .build());

        dispatcher.register(Set.of(command0, command1));
        final List<CommandCompletion> completions = dispatcher.complete(new Object(), input);
        assertContainsAll(completions(expected, lastInput), completions);
    }

    @ParameterizedTest
    @CsvSource({
            "'test hello ',--color|-c|--stringflag|-s|--boolflag|-b,''",
            "test hello a,'',a",
            "test hello argname,'',argname",
            "test hello --,--color|--stringflag|--boolflag,--",
            "test hello --c,--color,--c",
            "test hello -,--color|-c|--stringflag|-s|--boolflag|-b,-",
            "test hello argname --color,--color,--color",
            "test hello argname -c,-cs|-c,-c",
            "'test hello argname --color ',#,''",
            "test hello argname --color #,#0|#1|#2|#3|#4|#5|#6|#7|#8|#9|#a|#b|#c|#d|#e|#f,#",
            "test hello argname -c #,#0|#1|#2|#3|#4|#5|#6|#7|#8|#9|#a|#b|#c|#d|#e|#f,#",
            "test hello argname -c #ffffff,'#ffffff',#ffffff",
            "'test hello argname -b -c #ae43ff ',--stringflag|-s,''",
            "'test hello --color #ffffff -b argname -s asd ','',''",
            "test hello --color #ffffff -s,'-s',-s",
            "'test hello --color #ffffff -s ','',''",
            "test hello --color #fff,#fff0|#fff1|#fff2|#fff3|#fff4|#fff5|#fff6|#fff7|#fff8|#fff9|#fffa|#fffb|#fffc|#fffd|#fffe|#ffff,#fff",
            "test hello argname -b -c #fff,#fff0|#fff1|#fff2|#fff3|#fff4|#fff5|#fff6|#fff7|#fff8|#fff9|#fffa|#fffb|#fffc|#fffd|#fffe|#ffff,#fff",
    })
    public void complete_arguments(final String input, final String expected, final String lastInput) {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .eagerFlagCompletions()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("testcommand").aliases("testcmd", "test", "ts").build())
                .then(factory.literal("hello").aliases("hl").build())
                .arguments()
                .then(factory.required("stringarg", String.class).mapWith(word()).build())
                .flags()
                .then(factory.valueFlag("color", String.class).assumeShorthand().mapWith(new ColorArgumentMapper()).build())
                .then(factory.valueFlag("stringflag", String.class).assumeShorthand().mapWith(word()).build())
                .then(factory.boolFlag("boolflag").assumeShorthand().build())
                .build());

        dispatcher.register(command);
        final List<CommandCompletion> completions = dispatcher.complete(new Object(), input);
        assertContainsAll(completions(expected, lastInput), completions);
    }

    @ParameterizedTest
    @CsvSource({
            "'test hello ','',''",
            "test hello -,--color|-c|--stringflag|-s|--boolflag|-b,-",
            "test hello --,--color|--stringflag|--boolflag,--",
    })
    public void complete_nonEagerFlagCompletions(final String input, final String expected, final String lastInput) {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("testcommand").aliases("testcmd", "test", "ts").build())
                .then(factory.literal("hello").aliases("hl").build())
                .arguments()
                .then(factory.required("stringarg", String.class).mapWith(word()).build())
                .flags()
                .then(factory.valueFlag("color", String.class).assumeShorthand().mapWith(new ColorArgumentMapper()).build())
                .then(factory.valueFlag("stringflag", String.class).assumeShorthand().mapWith(word()).build())
                .then(factory.boolFlag("boolflag").assumeShorthand().build())
                .build());

        dispatcher.register(command);
        final List<CommandCompletion> completions = dispatcher.complete(new Object(), input);
        assertContainsAll(completions(expected, lastInput), completions);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a",
            "asd",
            "test b",
            "test hello abc",
            "test hello -x",
            "test hello --invalid-flag",
            "test hello -xyz",
            "test hello --z",
            "test hello abc abc",
            "test hello abc --color #xyzxyz",
            "'test hello abc --color #xyzxyz '",
            "'test hello abc -sb '",
            "test hello abc -sb --color #",
            "test hello abc --color #ffffff --color #,''"
    })
    public void complete_invalidArgument(final String input) {
        final DispatcherConfig<Object> config = DispatcherConfig.builder()
                .build();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.using(config);
        final CommandModule<Object> command = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("testcommand").aliases("testcmd", "test", "ts").build())
                .then(factory.literal("hello").aliases("hl").build())
                .arguments()
                .then(factory.required("stringarg", String.class).mapWith(word()).build())
                .flags()
                .then(factory.valueFlag("color", String.class).assumeShorthand().mapWith(new ColorArgumentMapper()).build())
                .then(factory.valueFlag("stringflag", String.class).assumeShorthand().mapWith(word()).build())
                .then(factory.boolFlag("boolflag").assumeShorthand().build())
                .build());

        dispatcher.register(command);
        final List<CommandCompletion> completions = dispatcher.complete(new Object(), input);
        assertIterableEquals(List.of(), completions);
    }
}
