package grapefruit.command.util;

import grapefruit.command.CommandModule;
import grapefruit.command.argument.CommandChain;
import grapefruit.command.argument.CommandChainFactory;
import grapefruit.command.dispatcher.CommandContext;

import java.util.function.Function;

public class TestCommandModule implements CommandModule<Object> {
    private final Function<CommandChainFactory<Object>, CommandChain<Object>> chainFactory;

    public TestCommandModule(final Function<CommandChainFactory<Object>, CommandChain<Object>> chainFactory) {
        this.chainFactory = chainFactory;
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
