package grapefruit.command.completion;

import grapefruit.command.dispatcher.CommandContext;

import java.util.List;

public interface CompletionSource<S> {

    List<Completion> complete(final CommandContext<S> context, final String input);
}
