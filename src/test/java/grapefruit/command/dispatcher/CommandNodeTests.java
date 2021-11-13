package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandNodeTests {

    @ParameterizedTest
    @ValueSource(strings = {"node", "name", "test", "command", "root", "command-node", "some_node", "cámmandnáde"})
    public void constructor_validNameAndAliasInput(final String name) {
        assertDoesNotThrow(() -> new CommandNode<>(name, Set.of(name), null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "$$", "§"})
    public void constructor_invalidNameAndAliasInput(final String name) {
        assertThrows(IllegalArgumentException.class, () -> new CommandNode<>(name, Set.of(name), null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"node", "name", "test", "command", "root", "command-node", "some_node", "cámmandnáde"})
    public void primary_equals(final String name) {
        final CommandNode<?> node = new CommandNode<>(name, Set.of(), null);
        assertEquals(name, node.primary());
    }

    @ParameterizedTest
    @CsvSource({"node,true", "name,false", "test,true", "command,false", "root,true", "command-node,false", "cámmandnáde,false"})
    public void aliases_contains(final String alias, final boolean shouldContain) {
        final String[] aliases = {"node", "test", "root"};
        final CommandNode<?> node = new CommandNode<>("some-node", aliases, null);
        assertEquals(shouldContain, node.aliases().contains(alias));
    }

    @ParameterizedTest
    @CsvSource({"node,true", "name,false", "test,true", "command,false", "root,true", "command-node,false", "cámmandnáde,false"})
    public void mergeAliases_contains(final String alias, final boolean shouldContain) {
        final CommandNode<?> firstNode = new CommandNode<>("somenode", Set.of(), null);
        final CommandNode<?> secondNode = new CommandNode<>("othernode", Set.of("node", "test", "root"), null);
        firstNode.mergeAliases(secondNode.aliases());
        assertEquals(shouldContain, firstNode.aliases().contains(alias));
    }

    @Test
    public void registration_isPresent() {
        final CommandNode<Object> node = new CommandNode<>("node", Set.of(), new DummyCommandRegistration());
        assertTrue(node.registration().isPresent());
    }

    @Test
    public void registration_isEmpty() {
        final CommandNode<?> node = new CommandNode<>("node", Set.of(), null);
        assertTrue(node.registration().isEmpty());
    }

    @Test
    public void setRegistration_validInput() {
        final CommandNode<Object> node = new CommandNode<>("node", Set.of(), null);
        node.registration(new DummyCommandRegistration());
        assertTrue(node.registration().isPresent());
    }

    @Test
    public void findChild_shouldFind() {
        final CommandNode<Object> parent = new CommandNode<>("parent", Set.of(), null);
        final CommandNode<Object> child = new CommandNode<>("child", Set.of(), null);
        parent.addChild(child);
        assertTrue(parent.findChild(child.primary()).isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"child", "other-child", "yet_another_child"})
    public void findChild_shouldNotFind(final String childName) {
        final CommandNode<?> parent = new CommandNode<>("parent", Set.of(), null);
        assertTrue(parent.findChild(childName).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"child", "other-child", "yet_another_child"})
    public void childrenSize_hasChild(final String childName) {
        final CommandNode<Object> parent = new CommandNode<>("parent", Set.of(), null);
        final CommandNode<Object> child = new CommandNode<>(childName, Set.of(), null);
        parent.addChild(child);
        assertEquals(1, parent.children().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"child", "other-child", "yet_another_child"})
    public void childrenContains_hasChild(final String childName) {
        final CommandNode<Object> parent = new CommandNode<>("parent", Set.of(), null);
        final CommandNode<Object> child = new CommandNode<>(childName, Set.of(), null);
        parent.addChild(child);
        assertTrue(parent.children().contains(child));
    }

    private static final class DummyCommandRegistration implements CommandRegistration<Object> {
        private static final TypeToken<Object> TYPE = TypeToken.of(Object.class);
        private static final CommandContainer HOLDER = new CommandContainer() {};

        @Override
        public CommandContainer holder() {
            return HOLDER;
        }

        @Override
        public Method method() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CommandParameter<Object>> parameters() {
            return List.of();
        }

        @Override
        public Optional<String> permission() {
            return Optional.empty();
        }

        @Override
        public Optional<TypeToken<?>> commandSourceType() {
            return Optional.of(TYPE);
        }

        @Override
        public boolean runAsync() {
            return false;
        }
    }
}
