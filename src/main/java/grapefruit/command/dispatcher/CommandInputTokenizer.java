package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class CommandInputTokenizer {

    public @NotNull Queue<CommandInput> tokenizeInput(final @NotNull String commandLine) {
        requireNonNull(commandLine, "commandLine cannot be null");
        final Queue<CommandInput> result = new ConcurrentLinkedQueue<>();
        final Queue<String> strings = Arrays.stream(commandLine.split(" "))
                .map(String::trim)
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
        String each;
        while ((each = strings.poll()) != null) {
            if (!each.isBlank()) {
                result.add(new StringCommandInput(each));
            } else {
                String blank;
                int blankCount = 0;
                while ((blank = strings.peek()) != null) {
                    if (!blank.isBlank()) {
                        break;
                    }

                    strings.remove();
                    blankCount++;
                }

                result.add(new BlankCommandInput(blankCount));
            }
        }

        return result;
    }
}
