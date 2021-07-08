package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class CommandResult {
    private final String commandLine;
    private final List<ParsedCommandArgument> parsedArguments;

    public CommandResult(final @NotNull String commandLine, final @NotNull List<ParsedCommandArgument> arguments) {
        this.commandLine = requireNonNull(commandLine, "commandLine cannot be null");
        this.parsedArguments = requireNonNull(arguments, "arguments cannot be null");
    }

    public @NotNull String commandLine() {
        return this.commandLine;
    }

    public @NotNull List<ParsedCommandArgument> parsedArguments() {
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

    public @NotNull ParsedCommandArgument findArgumentAt(final int index) {
        try {
            return this.parsedArguments.get(index);
        } catch (final IndexOutOfBoundsException ex) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public String toString() {
        return "CommandResult[" +
                "commandLine='" + this.commandLine + '\'' +
                ", parsedArguments=" + this.parsedArguments +
                ']';
    }
}
