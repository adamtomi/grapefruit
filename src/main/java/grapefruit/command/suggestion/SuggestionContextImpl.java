package grapefruit.command.suggestion;

import grapefruit.command.dispatcher.CommandContext;

import static java.util.Objects.requireNonNull;

final class SuggestionContextImpl<S> implements SuggestionContext<S> {
    private final CommandContext<S> commandContext;
    private final SuggestionFactory suggestionFactory;

    SuggestionContextImpl(final CommandContext<S> commandContext, final SuggestionFactory suggestionFactory) {
        this.commandContext = requireNonNull(commandContext, "commandContext cannot be null");
        this.suggestionFactory = requireNonNull(suggestionFactory, "suggestionFactory cannot be null");
    }

    @Override
    public CommandContext<S> commandContext() {
        return this.commandContext;
    }

    @Override
    public SuggestionFactory suggestionFactory() {
        return this.suggestionFactory;
    }
}
