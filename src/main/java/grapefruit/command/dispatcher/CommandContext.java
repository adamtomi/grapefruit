package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class CommandContext<S> {
    private final S source;
    private final String commandLine;
    private final List<ParsedCommandArgument> parsedArguments;

    public CommandContext(final @NotNull S source,
                          final @NotNull String commandLine,
                          final @NotNull List<ParsedCommandArgument> arguments) {
        this.source = requireNonNull(source, "source cannot be null");
        this.commandLine = requireNonNull(commandLine, "commandLine cannot be null");
        this.parsedArguments = requireNonNull(arguments, "arguments cannot be null");
    }

    public @NotNull S source() {
        return this.source;
    }

    public @NotNull String commandLine() {
        return this.commandLine;
    }

    public @NotNull List<ParsedCommandArgument> arguments() {
        return List.copyOf(this.parsedArguments);
    }

    public @NotNull ParsedCommandArgument findArgumentUnchecked(final @NotNull String name) {
        return findArgument(name).orElseThrow(NoSuchElementException::new);
    }

    public @NotNull Optional<ParsedCommandArgument> findArgument(final @NotNull String name) {
        requireNonNull(name, "name cannot be null");
        return this.parsedArguments.stream()
                .filter(arg -> arg.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public @NotNull Optional<ParsedCommandArgument> findArgumentAt(final int index) {
        try {
            return Optional.of(this.parsedArguments.get(index));
        } catch (final IndexOutOfBoundsException ex) {
            return Optional.empty();
        }
    }

    public @NotNull ParsedCommandArgument findArgumentAtUnsafe(final int index) {
        return findArgumentAt(index).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public String toString() {
        return "CommandContext[" +
                "source=" + this.source + '\'' +
                ", commandLine='" + this.commandLine + '\'' +
                ", parsedArguments=" + this.parsedArguments +
                ']';
    }
}
