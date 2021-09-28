package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unchecked")
public class CommandContext<S> {
    private final S source;
    private final String commandLine;
    private final Map<String, Object> arguments = new LinkedHashMap<>();

    public CommandContext(final @NotNull S source,
                          final @NotNull String commandLine) {
        this.source = requireNonNull(source, "source cannot be null");
        this.commandLine = requireNonNull(commandLine, "commandLine cannot be null");
    }

    public @NotNull S source() {
        return this.source;
    }

    public @NotNull String commandLine() {
        return this.commandLine;
    }

    public void put(final @NotNull String name, final @Nullable Object value) {
        this.arguments.put(name, value);
    }

    public void put(final int index, final @Nullable Object value) {
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be less than 0");
        }

        if (index >= argCount()) {
            throw new IllegalArgumentException(format("Index must not be greater than or equal to %s", argCount()));
        }

        // Is there a better way of doing this? I really hope...
        final Map.Entry<String, Object> entry = this.arguments.entrySet().stream()
                .skip(index)
                .findFirst()
                .orElseThrow();
        entry.setValue(value);
    }

    public @NotNull Map<String, Object> asMap() {
        // Map#copyOf doesn't allow null values... >:(
        return Collections.unmodifiableMap(this.arguments);
    }

    public int argCount() {
        return this.arguments.size();
    }

    public <T> @NotNull Optional<T> find(final @NotNull String name) {
        final @Nullable Object found = this.arguments.get(name);
        return Optional.ofNullable((T) found);
    }

    public <T> @NotNull T findUnchecked(final @NotNull String name) {
        return (T) find(name).orElseThrow(() -> new NoSuchElementException(format("Could not find argument with name '%s'", name)));
    }

    public <T> @NotNull Optional<T> findAt(final int index) {
        try {
            final @Nullable T result = (T) this.arguments.values().toArray(Object[]::new)[index];
            return Optional.ofNullable(result);
        } catch (final IndexOutOfBoundsException ex) {
            return Optional.empty();
        }
    }

    public <T> @NotNull T findAtUnchecked(final int index) {
        return (T) findAt(index).orElseThrow(() -> new NoSuchElementException(format("Could not find argument at index %s", index)));
    }

    @Override
    public String toString() {
        return "CommandContext[" +
                "source=" + this.source + '\'' +
                ", commandLine='" + this.commandLine + '\'' +
                ", arguments=" + this.arguments +
                ']';
    }
}
