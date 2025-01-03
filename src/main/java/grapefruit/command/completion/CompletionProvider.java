package grapefruit.command.completion;

import grapefruit.command.dispatcher.CommandContext;

@FunctionalInterface
public interface CompletionProvider<S> {

    CompletionAccumulator complete(final CommandContext<S> context, final CompletionBuilder builder);
}
