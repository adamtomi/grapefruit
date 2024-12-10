package grapefruit.command.mock;

import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.argument.CommandChainFactory;
import grapefruit.command.dispatcher.CommandContext;

import java.util.function.Function;

public class TestCommandModule implements CommandModule<Object> {
    private final Function<CommandChainFactory<Object>, CommandChain<Object>> chainFactory;

    private TestCommandModule(final Function<CommandChainFactory<Object>, CommandChain<Object>> chainFactory) {
        this.chainFactory = chainFactory;
    }

    public static TestCommandModule of(final Function<CommandChainFactory<Object>, CommandChain<Object>> chainFactory) {
        return new TestCommandModule(chainFactory);
    }

    public static TestCommandModule computed(final CommandChain<Object> chain) {
        return new TestCommandModule(x -> chain);
    }

    public static TestCommandModule dummy() {
        return new TestCommandModule(factory -> new EmptyCommandChain());
    }

    @Override
    public CommandChain<Object> chain(final CommandChainFactory<Object> factory) {
        return this.chainFactory.apply(factory);
    }

    @Override
    public void execute(final CommandContext<Object> context) {
        // Do nothing
    }
}
