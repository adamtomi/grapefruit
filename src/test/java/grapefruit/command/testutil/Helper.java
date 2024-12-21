package grapefruit.command.testutil;

import grapefruit.command.argument.mapper.CommandInputAccess;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;

import java.util.Arrays;
import java.util.List;

public final class Helper {
    private Helper() {}

    public static List<String> toStringList(final String input) {
        if (input.isEmpty()) return List.of();
        return Arrays.asList(input.split("\\|"));
    }

    public static CommandInputAccess access(final String input) {
        return CommandInputAccess.wrap(CommandInputTokenizer.wrap(input));
    }
}