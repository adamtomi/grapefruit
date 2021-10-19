package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.CommandDefinition;
import grapefruit.command.dispatcher.listener.PreDispatchListener;
import grapefruit.command.dispatcher.listener.PreProcessLitener;
import grapefruit.command.parameter.modifier.Flag;
import grapefruit.command.parameter.modifier.OptParam;
import grapefruit.command.parameter.modifier.Source;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandDispatcherTests {

    @Test
    public void registerCommand_staticMethod() {
        final CommandDispatcher<?> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        assertThrows(IllegalStateException.class, () -> dispatcher.registerCommands(new ContainerWithStaticMethod()));
    }

    @Test
    public void registerCommand_privateMethod() {
        final CommandDispatcher<?> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        assertDoesNotThrow(() -> dispatcher.registerCommands(new ContainerWithPrivateMethod()));
    }

    @Test
    public void registerCommand_invalidRedirectAnnot() {
        final CommandDispatcher<?> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        assertThrows(IllegalArgumentException.class, () -> dispatcher.registerCommands(new ContainerWithInvalidRedirectAnnot()));
    }

    @Test
    public void registerCommand_illegalCommandSourceType() {
        final CommandDispatcher<CommandSource> dispatcher = CommandDispatcher.builder(TypeToken.of(CommandSource.class))
                .build();
        assertThrows(RuntimeException.class, () -> dispatcher.registerCommands(new OrdinaryContainer()));
    }

    @Test
    public void registerCommand_registrationHandler() {
        final AtomicBoolean regHandlerInvoked = new AtomicBoolean(false);
        final CommandDispatcher<?> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .withRegistrationHandler(context -> regHandlerInvoked.set(true))
                .build();
        dispatcher.registerCommands(new OrdinaryContainer());
        assertTrue(regHandlerInvoked.get());
    }

    @Test
    public void registerCommand_registrationHandlerWithRedirectNodes() {
        final AtomicInteger regHandlerStatus = new AtomicInteger(0);
        final CommandDispatcher<?> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .withRegistrationHandler(context -> regHandlerStatus.incrementAndGet())
                .build();
        dispatcher.registerCommands(new ContainerWithValidRedirectAnnot());
        assertEquals(2, regHandlerStatus.get());
    }

    @Test
    public void dispatchCommand_noArguments() {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final StatusAwareContainer container = new ContainerWithASingleCommand();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(new Object(), "test");
        assertTrue(container.status);
    }

    @Test
    public void dispatchCommand_preProcessListener() {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final StatusAwareContainer container = new ContainerWithASingleCommand();
        dispatcher.registerCommands(container);
        dispatcher.registerListener((PreProcessLitener<Object>) (source, commandLine) -> false);
        dispatcher.dispatchCommand(new Object(), "test");
        assertFalse(container.status);
    }

    @Test
    public void dispatchCommand_preDispatchListener() {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final StatusAwareContainer container = new ContainerWithASingleCommand();
        dispatcher.registerCommands(container);
        dispatcher.registerListener((PreDispatchListener<Object>) (context, registration) -> false);
        dispatcher.dispatchCommand(new Object(), "test");
        assertFalse(container.status);
    }

    @Test
    public void dispatchCommand_postDispatchListener() {
        final AtomicBoolean listenerStatus = new AtomicBoolean(false);
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final StatusAwareContainer container = new ContainerWithASingleCommand();
        dispatcher.registerCommands(container);
        dispatcher.registerListener(context -> listenerStatus.set(true));
        dispatcher.dispatchCommand(new Object(), "test");
        assertTrue(listenerStatus.get());
    }

    @ParameterizedTest
    @ValueSource(classes = {TerminalCommandSource.class, GeneralCommandSource.class})
    public void dispatchCommand_variousCommandSourceTypes(final Class<CommandSource> clazz) throws ReflectiveOperationException {
        final CommandSource source = clazz.getConstructor().newInstance();
        final CommandDispatcher<CommandSource> dispatcher = CommandDispatcher.builder(TypeToken.of(CommandSource.class))
                .build();
        final StatusAwareContainer container = new ContainerWithVariousSourceTypes();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(source, "test0");
        assertTrue(container.status);
    }

    @Test
    public void dispatchCommand_terminalOnly() {
        final CommandDispatcher<CommandSource> dispatcher = CommandDispatcher.builder(TypeToken.of(CommandSource.class))
                .build();
        final StatusAwareContainer container = new ContainerWithVariousSourceTypes();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(new GeneralCommandSource(), "test2");
        assertFalse(container.status);
    }

    @Test
    public void dispatchCommand_generalOnly() {
        final CommandDispatcher<CommandSource> dispatcher = CommandDispatcher.builder(TypeToken.of(CommandSource.class))
                .build();
        final StatusAwareContainer container = new ContainerWithVariousSourceTypes();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(new TerminalCommandSource(), "test1");
        assertFalse(container.status);
    }

    @Test
    public void dispatchCommand_redirectNode() {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final StatusAwareContainer container = new ContainerWithValidRedirectAnnot();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(new Object(), "b");
        assertTrue(container.status);
    }

    @ParameterizedTest
    @CsvSource({"Hello!,true","Some-string,false"})
    public void dispatchCommand_redirectNodeWithParameters(final String argument, final boolean redirectNode) {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final ContainerWithParameterizedRedirectNode container = new ContainerWithParameterizedRedirectNode();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(new Object(), !redirectNode ? "a b c " + argument : "d");
        assertEquals(argument, container.string);
    }

    @Test
    public void dispatchCommand_noPermission() {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .withAuthorizer((source, permission) -> false)
                .build();
        final StatusAwareContainer container = new ContainerWithASingleCommand();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(new Object(), "test");
        assertFalse(container.status);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "root method01 --flag -123.0 --other-flag 1400 Hello! 4",
            "root method01 --other-flag 45000 --flag 123.0 Hello! 5",
            "root method01 Hey! --flag 45.653 5 --other-flag 67540",
            "root method01 Some-string --other-flag -675 76 --flag 98.989898",
            "root method01 another_string 18 --flag 56.4343434 --other-flag 45000",
            "root method01 this-is-a-string 92 --other-flag 1000000 --flag 43.10121",
            "root method02 -f a -o -968.353 100 -a c",
            "root method02 -fo 968.353 a 100 -a r",
            "root method02 -fa b a 100 -o 462.475",
            "root method02 -fa b a 100 --other-flag 462.475",
            "root method02 -fao c 45.123 a 450",
            "root method02 string -ofa 987.654 e 140"
    })
    public void dispatchCommand_complex_validInput(final String commandLine) {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final StatusAwareContainer container = new ContainerWithComplexCommands();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(new Object(), commandLine);
        assertTrue(container.status);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "root --flag method01 123.0 --other-flag 1400 Hello! 4",
            "root method01 --other-flag --flag 123.0 Hello! 5",
            "root method01 Hey! --flag 45.653 --other-flag 67540",
            "root method01 --other-flag 675 76 --flag 98.989898 Some-string ",
            "root method01 another_string abc",
            "root method01",
            "root method01 string 18 --",
            "root method01 string 18 --flag",
            "root method02",
            "root method02 some_string 48 -f -f",
            "root method02 -a c --another-flag d",
            "root method02 -a c --another-flag d some-other-string 14",
            "root method02 -asd 198",
            "root method02 this-is-a-string -fao 45"
    })
    public void dispatchCommand_complex_invalidInput(final String commandLine) {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final StatusAwareContainer container = new ContainerWithComplexCommands();
        dispatcher.registerCommands(container);
        dispatcher.dispatchCommand(new Object(), commandLine);
        assertFalse(container.status);
    }

    @ParameterizedTest
    @CsvSource({
            "roo,root",
            "'root ',method01|method02",
            "'root method01 --flag ',-9|-8|-7|-6|-5|-4|-3|-2|-1|1|2|3|4|5|6|7|8|9",
            //"root method01 --flag 1,10|11|12|13|14|15|16|17|18|19",
            "root method01 Hello -,-9|-8|-7|-6|-5|-4|-3|-2|-1|--flag|--other-flag",
            "root method01 Hello --flag 34 -,-9|-8|-7|-6|-5|-4|-3|-2|-1|--other-flag",
            "root method01 -,--flag|--other-flag",
            "root method01 --,--flag|--other-flag",
            "root method01 --f,--flag",
            //"root method01 --flag 4 --other-flag 444444 str 5,50|51|52|53|54|55|56|57|58|59",
            "root method02 -f,-fa|-fo",
            "'root method02 -fao b ',-9|-8|-7|-6|-5|-4|-3|-2|-1|1|2|3|4|5|6|7|8|9",
            "root method02 Hey_there! -,-9|-8|-7|-6|-5|-4|-3|-2|-1|-f|-o|-a|--flag|--other-flag|--another-flag",
            //"root method02 Hey_there! -3,-30|-31|-32|-33|-34|-35|-36|-37|-38|-39",
            "root method02 hello -345 -a,-af|-ao",
            "root method02 -f hello -345 -a c -,-o|--other-flag",
            "'root method02 -ao ',"
    })
    public void listSuggestions_validInput(final String commandLine, final String expectedString) {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        dispatcher.registerCommands(new ContainerWithComplexCommands());
        final List<String> expected = expectedString == null ? List.of() : Arrays.asList(expectedString.split("\\|"));
        final List<String> result = dispatcher.listSuggestions(new Object(), commandLine);
        assertTrue(contentEquals(expected, result));
    }

    private static boolean contentEquals(final List<String> expected, final List<String> result) {
        if (expected.size() != result.size()) {
            return false;
        }

        for (final String each : expected) {
            if (!result.contains(each)) {
                return false;
            }
        }

        return true;
    }

    /* Command container classes */
    private static final class ContainerWithStaticMethod implements CommandContainer {
        @CommandDefinition(route = "a")
        public static void method() {}
    }

    private static final class ContainerWithPrivateMethod implements CommandContainer {
        @CommandDefinition(route = "a")
        private void method() {}
    }

    private static final class ContainerWithInvalidRedirectAnnot implements CommandContainer {
        @Redirect(from = "a")
        @CommandDefinition(route = "a")
        public void method() {}
    }

    private static final class OrdinaryContainer implements CommandContainer {
        @CommandDefinition(route = "a|b|c")
        public void method(final @Source Object source) {}
    }

    private static abstract class StatusAwareContainer implements CommandContainer {
        protected boolean status = false;
    }

    private static final class ContainerWithValidRedirectAnnot extends StatusAwareContainer {
        @Redirect(from = "a")
        @CommandDefinition(route = "b")
        public void method() {
            this.status = true;
        }
    }

    private static final class ContainerWithASingleCommand extends StatusAwareContainer {
        @CommandDefinition(route = "test", permission = "some.permission")
        public void method() {
            this.status = true;
        }
    }

    private static final class ContainerWithVariousSourceTypes extends StatusAwareContainer {
        @CommandDefinition(route = "test0")
        public void generic(final @Source CommandSource source) {
            this.status = true;
        }

        @CommandDefinition(route = "test1")
        public void generalOnly(final @Source GeneralCommandSource source) {
            this.status = true;
        }

        @CommandDefinition(route = "test2")
        public void terminalOnly(final @Source TerminalCommandSource source) {
            this.status = true;
        }
    }

    private static final class ContainerWithParameterizedRedirectNode extends StatusAwareContainer {
        private String string;

        @Redirect(from = "d", arguments = "Hello!")
        @CommandDefinition(route = "a b c")
        public void method(final @OptParam String str) {
            this.string = str;
        }
    }

    private static final class ContainerWithComplexCommands extends StatusAwareContainer {
        @CommandDefinition(route = "root method01")
        public void method01(final String str,
                             final @Flag("flag") double d,
                             final short s,
                             final @Flag("other-flag") long o) {
            this.status = true;
        }

        @CommandDefinition(route = "root method02")
        public void method02(final String str,
                             final @Flag(value = "flag", shorthand = 'f') boolean b,
                             final @Flag(value = "other-flag", shorthand = 'o') double d,
                             final @OptParam long l,
                             final @Flag(value = "another-flag", shorthand = 'a') char c) {
            this.status = true;
        }
    }

    /* Command source classes */
    private interface CommandSource {}

    private static final class TerminalCommandSource implements CommandSource {
        public TerminalCommandSource() {}
    }

    private static final class GeneralCommandSource implements CommandSource {
        public  GeneralCommandSource() {}
    }
}
