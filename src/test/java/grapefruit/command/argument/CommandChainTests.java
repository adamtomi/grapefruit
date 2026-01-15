package grapefruit.command.argument;

import org.junit.jupiter.api.Test;

import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.greedy;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.word;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandChainTests {

    @Test
    public void literal_emptyRoute() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain().build());
    }

    @Test
    public void literal_invalidNameAndAlias() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalArgumentException.class, () -> factory.newChain().then(factory.literal("a b").build()).build());
        assertThrows(IllegalArgumentException.class, () -> factory.newChain().then(factory.literal("a").aliases("b c").build()).build());
    }

    @Test
    public void argument_emptyRoute() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain().arguments().build());
    }

    @Test
    public void argument_duplicateArgumentNames() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain().then(factory.literal("test").build())
                .arguments()
                .then(factory.required("test", String.class).mapWith(word()).build())
                .then(factory.required("test", String.class).mapWith(word()).build())
                .build());
    }

    @Test
    public void argument_mapperIsRequired() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(NullPointerException.class, () -> factory.newChain().then(factory.literal("test").build())
                .arguments()
                .then(factory.required("test", String.class).build())
                .build());
    }

    @Test
    public void argument_cannotRegisterAfterTerminal() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain().then(factory.literal("test").build())
                .arguments()
                .then(factory.required("test", String.class).mapWith(greedy()).build())
                .then(factory.required("hello", String.class).mapWith(word()).build())
                .build());
    }

    @Test
    public void argument_noFlagAfterTerminal() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain().then(factory.literal("test").build())
                .arguments()
                .then(factory.required("test", String.class).mapWith(greedy()).build())
                .flags()
                .then(factory.boolFlag("test").build())
                .build());
    }

    @Test
    public void flag_emptyRoute() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain().flags().build());
    }

    @Test
    public void flag_emptyRoute_emptyArguments() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain().arguments().flags().build());
    }

    @Test
    public void flag_noTerminalArgumentMapper() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain()
                .then(factory.literal("test").build())
                .flags()
                .then(factory.valueFlag("test2", String.class).mapWith(greedy()).build())
                .build());
    }

    @Test
    public void flag_argumentWithNameExists() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain()
                .then(factory.literal("test").build())
                .arguments()
                .then(factory.required("arg", String.class).mapWith(word()).build())
                .flags()
                .then(factory.boolFlag("arg").build())
                .build());
    }

    @Test
    public void flag_duplicateFlagName() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain()
                .then(factory.literal("test").build())
                .arguments()
                .flags()
                .then(factory.valueFlag("arg", String.class).mapWith(word()).build())
                .then(factory.boolFlag("arg").build())
                .build());
    }

    @Test
    public void flag_illegalShorthand() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalArgumentException.class, () -> factory.newChain()
                .then(factory.literal("test").build())
                .arguments()
                .flags()
                .then(factory.boolFlag("arg").shorthand('$').build())
                .build());
    }

    @Test
    public void flag_duplicateFlagShorthands() {
        final CommandChainFactory<Object> factory = CommandChain.factory();
        assertThrows(IllegalStateException.class, () -> factory.newChain()
                .then(factory.literal("test").build())
                .arguments()
                .flags()
                .then(factory.valueFlag("arg", String.class).assumeShorthand().mapWith(word()).build())
                .then(factory.boolFlag("hello").shorthand('a').build())
                .build());
    }
}
