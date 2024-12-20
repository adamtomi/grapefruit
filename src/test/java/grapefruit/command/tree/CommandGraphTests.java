package grapefruit.command.tree;

import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.argument.CommandChainFactory;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.dispatcher.CommandSyntaxException;
import grapefruit.command.mock.EmptyCommandChain;
import grapefruit.command.mock.TestCommandModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandGraphTests {

    @Test
    public void insert_emptyTree() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChain<Object> chain = new EmptyCommandChain();

        assertThrows(IllegalStateException.class, () -> graph.insert(chain, TestCommandModule.dummy()));
    }

    @Test
    public void insert_ambiguousTree() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandModule<Object> module = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = module.chain(factory);

        assertDoesNotThrow(() -> graph.insert(chain, module));
        assertThrows(IllegalStateException.class, () -> graph.insert(chain, TestCommandModule.dummy()));
    }

    @Test
    public void insert_success() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandModule<Object> module = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = module.chain(factory);

        graph.insert(chain, module);
        assertDoesNotThrow(() -> assertEquals(module, graph.search(CommandInputTokenizer.wrap("test"))));
    }

    @Test
    public void insert_treeIntegrity() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChainFactory<Object> factory = CommandChain.factory();

        final CommandChain<Object> chain1 = factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("other").build()).build();

        final CommandChain<Object> chain2 = factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("next").build()).build();

        final CommandChain<Object> chain3 = factory.newChain()
                .then(factory.literal("command").build()).build();

        final CommandModule<Object> command1 = TestCommandModule.computed(chain1);
        final CommandModule<Object> command2 = TestCommandModule.computed(chain2);
        final CommandModule<Object> command3 = TestCommandModule.computed(chain3);

        graph.insert(chain1, command1);
        graph.insert(chain2, command2);
        graph.insert(chain3, command3);

        assertDoesNotThrow(() -> assertEquals(command1, graph.search(CommandInputTokenizer.wrap("test other"))));
        assertDoesNotThrow(() -> assertEquals(command2, graph.search(CommandInputTokenizer.wrap("test next"))));
        assertDoesNotThrow(() -> assertEquals(command3, graph.search(CommandInputTokenizer.wrap("command"))));
    }

    @Test
    public void delete_noSuchChild_emptyTree() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = factory.newChain()
                .then(factory.literal("test").build()).build();

        assertThrows(IllegalStateException.class, () -> graph.delete(chain));
    }

    @Test
    public void delete_noSuchChild() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChainFactory<Object> factory = CommandChain.factory();

        final CommandChain<Object> chain1 = factory.newChain()
                .then(factory.literal("test").build()).build();
        final CommandChain<Object> chain2 = factory.newChain()
                        .then(factory.literal("other").build()).build();

        graph.insert(chain1, TestCommandModule.dummy());
        assertThrows(IllegalStateException.class, () -> graph.delete(chain2));
    }

    @Test
    public void delete_nonLeafNode() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChainFactory<Object> factory = CommandChain.factory();

        final CommandChain<Object> chain1 = factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("next").build())
                .build();

        final CommandChain<Object> chain2 = factory.newChain()
                .then(factory.literal("test").build()).build();

        graph.insert(chain1, TestCommandModule.dummy());
        assertThrows(IllegalStateException.class, () -> graph.delete(chain2));
    }

    @Test
    public void delete_success() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = factory.newChain()
                .then(factory.literal("test").build()).build();

        final CommandModule<Object> command = TestCommandModule.computed(chain);
        graph.insert(chain, command);
        graph.delete(chain);

        assertThrows(NoSuchCommandException.class, () -> graph.search(CommandInputTokenizer.wrap("test")));
    }

    @Test
    public void delete_treeIntegrity() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChainFactory<Object> factory = CommandChain.factory();

        final CommandChain<Object> chain1 = factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("other").build()).build();

        final CommandChain<Object> chain2 = factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("next").build()).build();

        final CommandChain<Object> chain3 = factory.newChain()
                .then(factory.literal("command").build()).build();

        final CommandModule<Object> command1 = TestCommandModule.computed(chain1);
        final CommandModule<Object> command2 = TestCommandModule.computed(chain2);
        final CommandModule<Object> command3 = TestCommandModule.computed(chain3);

        graph.insert(chain1, command1);
        graph.insert(chain2, command2);
        graph.insert(chain3, command3);

        graph.delete(chain1);
        assertThrows(NoSuchCommandException.class, () -> graph.search(CommandInputTokenizer.wrap("test other")));
        assertDoesNotThrow(() -> assertEquals(command2, graph.search(CommandInputTokenizer.wrap("test next"))));
        assertDoesNotThrow(() -> assertEquals(command3, graph.search(CommandInputTokenizer.wrap("command"))));
    }

    @Test
    public void search_emptyTree() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        assertThrows(NoSuchCommandException.class, () -> graph.search(CommandInputTokenizer.wrap("test")));
    }

    @Test
    public void search_emptyTree_emptyInput() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        assertThrows(NoSuchCommandException.class, () -> graph.search(CommandInputTokenizer.wrap("")));
    }

    @Test
    public void search_noSuchCommand() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = factory.newChain()
                .then(factory.literal("test").build()).build();

        graph.insert(chain, TestCommandModule.dummy());
        assertThrows(NoSuchCommandException.class, () -> graph.search(CommandInputTokenizer.wrap("hello")));
    }

    @Test
    public void search_success() {
        final CommandGraph<Object> graph = new CommandGraph<>();
        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = factory.newChain()
                .then(factory.literal("test").build()).build();
        final CommandModule<Object> command = TestCommandModule.computed(chain);

        graph.insert(chain, command);
        assertDoesNotThrow(() -> assertEquals(command, graph.search(CommandInputTokenizer.wrap("test"))));
    }
}
