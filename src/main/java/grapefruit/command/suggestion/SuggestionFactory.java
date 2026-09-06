package grapefruit.command.suggestion;

public interface SuggestionFactory {

    Suggestion create(final String input);

    static SuggestionFactory defaultFactory() {
        return SuggestionImpl::new;
    }
}
