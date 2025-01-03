package grapefruit.command.tree;

import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.argument.CommandChainFactory;
import grapefruit.command.completion.CommandCompletion;
import grapefruit.command.completion.CompletionFactory;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.mock.EmptyCommandChain;
import grapefruit.command.mock.TestCommandModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandGraphTests {
    
    private static CommandGraph<Object> graph() {
        return new CommandGraph<>(CommandCompletion.factory());
    }

    @Test
    public void insert_emptyTree() {
        final CommandGraph<Object> graph = graph();
        final CommandChain<Object> chain = new EmptyCommandChain();

        assertThrows(IllegalStateException.class, () -> graph.insert(chain, TestCommandModule.dummy()));
    }

    @Test
    public void insert_ambiguousTree() {
        final CommandGraph<Object> graph = graph();
        final CommandModule<Object> module0 = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        final CommandModule<Object> module1 = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build())
                .then(factory.literal("nested").build())
                .build());

        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain0 = module0.chain(factory);
        final CommandChain<Object> chain1 = module1.chain(factory);

        assertDoesNotThrow(() -> graph.insert(chain0, module0));
        assertThrows(IllegalStateException.class, () -> graph.insert(chain0, TestCommandModule.dummy()));
        assertThrows(IllegalStateException.class, () -> graph.insert(chain1, module1));

    }

    @Test
    public void insert_success() {
        final CommandGraph<Object> graph = graph();
        final CommandModule<Object> module = TestCommandModule.of(factory -> factory.newChain()
                .then(factory.literal("test").build()).build());

        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = module.chain(factory);

        graph.insert(chain, module);
        assertDoesNotThrow(() -> assertEquals(module, graph.query(CommandInputTokenizer.wrap("test"))));
    }

    @Test
    public void insert_treeIntegrity() {
        final CommandGraph<Object> graph = graph();
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

        assertDoesNotThrow(() -> assertEquals(command1, graph.query(CommandInputTokenizer.wrap("test other"))));
        assertDoesNotThrow(() -> assertEquals(command2, graph.query(CommandInputTokenizer.wrap("test next"))));
        assertDoesNotThrow(() -> assertEquals(command3, graph.query(CommandInputTokenizer.wrap("command"))));
    }

    @Test
    public void delete_noSuchChild_emptyTree() {
        final CommandGraph<Object> graph = graph();
        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = factory.newChain()
                .then(factory.literal("test").build()).build();

        assertThrows(IllegalStateException.class, () -> graph.delete(chain));
    }

    @Test
    public void delete_noSuchChild() {
        final CommandGraph<Object> graph = graph();
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
        final CommandGraph<Object> graph = graph();
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
        final CommandGraph<Object> graph = graph();
        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = factory.newChain()
                .then(factory.literal("test").build()).build();

        final CommandModule<Object> command = TestCommandModule.computed(chain);
        graph.insert(chain, command);
        graph.delete(chain);

        assertThrows(NoSuchCommandException.class, () -> graph.query(CommandInputTokenizer.wrap("test")));
    }

    @Test
    public void delete_treeIntegrity() {
        final CommandGraph<Object> graph = graph();
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
        assertThrows(NoSuchCommandException.class, () -> graph.query(CommandInputTokenizer.wrap("test other")));
        assertDoesNotThrow(() -> assertEquals(command2, graph.query(CommandInputTokenizer.wrap("test next"))));
        assertDoesNotThrow(() -> assertEquals(command3, graph.query(CommandInputTokenizer.wrap("command"))));
    }

    @Test
    public void query_emptyTree() {
        final CommandGraph<Object> graph = graph();
        assertThrows(NoSuchCommandException.class, () -> graph.query(CommandInputTokenizer.wrap("test")));
    }

    @Test
    public void query_emptyTree_emptyInput() {
        final CommandGraph<Object> graph = graph();
        assertThrows(NoSuchCommandException.class, () -> graph.query(CommandInputTokenizer.wrap("")));
    }

    @Test
    public void query_noSuchCommand() {
        final CommandGraph<Object> graph = graph();
        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = factory.newChain()
                .then(factory.literal("test").build()).build();

        graph.insert(chain, TestCommandModule.dummy());
        assertThrows(NoSuchCommandException.class, () -> graph.query(CommandInputTokenizer.wrap("hello")));
    }

    @Test
    public void query_success() {
        final CommandGraph<Object> graph = graph();
        final CommandChainFactory<Object> factory = CommandChain.factory();
        final CommandChain<Object> chain = factory.newChain()
                .then(factory.literal("test").build()).build();
        final CommandModule<Object> command = TestCommandModule.computed(chain);

        graph.insert(chain, command);
        assertDoesNotThrow(() -> assertEquals(command, graph.query(CommandInputTokenizer.wrap("test"))));
    }
}
