package grapefruit.command;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.argument.CommandChainFactory;
import grapefruit.command.dispatcher.CommandContext;

public interface CommandModule<S> {

    CommandChain<S> chain(final CommandChainFactory<S> factory);

    void execute(final CommandContext<S> context);
}
