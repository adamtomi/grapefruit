package grapefruit.command.testutil;

import java.util.Arrays;
import java.util.List;

public final class Helper {
    private Helper() {}

    public static List<String> toStringList(final String input) {
        if (input.isEmpty()) return List.of();
        return Arrays.asList(input.split("\\|"));
    }
}
