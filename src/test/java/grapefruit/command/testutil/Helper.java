package grapefruit.command.testutil;

import grapefruit.command.completion.Completion;
import grapefruit.command.completion.CompletionSupport;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;

import java.util.List;

public final class Helper {
    private Helper() {}

    public static List<Completion> completions(final String input) {
        if (input.isEmpty()) return List.of();
        return CompletionSupport.strings(input.split("\\|"));
    }

    public static CommandInputTokenizer inputOf(final String input) {
        return CommandInputTokenizer.wrap(input);
    }
}
