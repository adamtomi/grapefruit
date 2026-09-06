package grapefruit.command.testutil;

import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.suggestion.Suggestion;
import grapefruit.command.suggestion.SuggestionFactory;
import grapefruit.command.util.StringUtil;

import java.util.Arrays;
import java.util.List;

public final class Helper {
    private Helper() {}

    public static List<Suggestion> suggestions(final String expected, final String input) {
        if (expected.isEmpty()) return List.of();

        return Arrays.stream(expected.split("\\|"))
                .filter(x -> StringUtil.startsWithIgnoreCase(x, input))
                .map(SuggestionFactory.defaultFactory()::create)
                .toList();
    }

    public static CommandInputTokenizer inputOf(final String input) {
        return CommandInputTokenizer.wrap(input);
    }
}
