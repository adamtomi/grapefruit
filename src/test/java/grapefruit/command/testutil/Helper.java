package grapefruit.command.testutil;

import grapefruit.command.completion.CommandCompletion;
import grapefruit.command.completion.CompletionBuilder;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;

import java.util.List;

public final class Helper {
    private Helper() {}

    public static List<CommandCompletion> completions(final String expected, final String input) {
        if (expected.isEmpty()) return List.of();
        return CompletionBuilder.of(CommandCompletion.factory(), input)
                .includeStrings(expected.split("\\|"))
                .build()
                .filterCompletions();
    }

    public static CommandInputTokenizer inputOf(final String input) {
        return CommandInputTokenizer.wrap(input);
    }
}
