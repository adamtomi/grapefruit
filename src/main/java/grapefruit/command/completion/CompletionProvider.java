package grapefruit.command.completion;

import grapefruit.command.dispatcher.CommandContext;

import java.util.List;

@FunctionalInterface
public interface CompletionProvider<S> {

    CommandCompletion complete(final CommandContext<S> context, final CompletionBuilder builder);
}
