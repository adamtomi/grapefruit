package grapefruit.command.suggestion;

import grapefruit.command.dispatcher.CommandContext;

public interface SuggestionContext<S> {

    CommandContext<S> commandContext();

    SuggestionFactory suggestionFactory();

    static <S> SuggestionContext<S> create(final CommandContext<S> commandContext, final SuggestionFactory suggestionFactory) {
        return new SuggestionContextImpl<>(commandContext, suggestionFactory);
    }
}
