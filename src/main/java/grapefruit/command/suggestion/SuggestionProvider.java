package grapefruit.command.suggestion;


import java.util.stream.Stream;

public interface SuggestionProvider<S> {

    default Stream<Suggestion> suggest(final SuggestionContext<S> context, final String input) {
        return suggestStrings(context, input).map(context.suggestionFactory()::create);
    }

    default Stream<String> suggestStrings(final SuggestionContext<S> context, final String input) {
        return Stream.of();
    }
}
