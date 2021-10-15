package grapefruit.command.dispatcher;

import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unchecked")
public class CommandContext<S> {
    private static final Comparator<Map.Entry<Integer, String>> SORT_BY_INDEX = Comparator.comparingInt(Map.Entry::getKey);
    private final SuggestionContext<S> suggestions = new SuggestionContext<>();
    private final S source;
    private final String commandLine;
    private final Map<Integer, String> indexStore;
    private final Map<String, StoredValue> argumentStore;

    @VisibleForTesting
    protected CommandContext(final @NotNull S source,
                             final @NotNull String commandLine,
                             final @NotNull Map<Integer, String> indexStore,
                             final @NotNull Map<String, StoredValue> argumentStore) {
        this.source = requireNonNull(source, "source cannot be null");
        this.commandLine = requireNonNull(commandLine, "commandLine cannot be null");
        this.indexStore = requireNonNull(indexStore, "indexStore cannot be null");
        this.argumentStore = requireNonNull(argumentStore, "argumentStore cannot be null");
    }

    public static <S> @NotNull CommandContext<S> create(final @NotNull S source,
                                                        final @NotNull String commandLine,
                                                        final @NotNull List<CommandParameter<S>> params) {
        final Map<Integer, String> indexStore = new ConcurrentHashMap<>();
        final Map<String, StoredValue> defaults = new ConcurrentHashMap<>();
        for (int i = 0; i < params.size(); i++) {
            indexStore.put(i, Miscellaneous.parameterName(params.get(i)));
        }

        for (final CommandParameter<S> parameter : params) {
            final String name = Miscellaneous.parameterName(parameter);
            final Class<?> type = parameter.type().getRawType();
            final Object defaultValue = type.isPrimitive()
                    ? Miscellaneous.nullToPrimitive(type)
                    : null;
            defaults.put(name, new StoredValue(defaultValue, false));
        }
        return new CommandContext<>(source, commandLine, indexStore, defaults);
    }

    @NotNull SuggestionContext<S> suggestions() {
        return this.suggestions;
    }

    public @NotNull S source() {
        return this.source;
    }

    public @NotNull String commandLine() {
        return this.commandLine;
    }

    public void put(final @NotNull String name, final @Nullable Object value) {
        this.argumentStore.put(name, new StoredValue(value, true));
    }

    public @NotNull Map<String, Object> asMap() {
        final Map<String, Object> result = new LinkedHashMap<>(argCount());
        this.indexStore.entrySet().stream()
                .sorted(SORT_BY_INDEX)
                .map(Map.Entry::getValue)
                .forEach(name -> {
                    if (!this.argumentStore.containsKey(name)) {
                        throw new IllegalArgumentException(format("No value found for name '%s'", name));
                    }

                    result.put(name, this.argumentStore.get(name).value());
                });

        return result;
    }

    public int argCount() {
        return this.argumentStore.size();
    }

    public <T> @NotNull Optional<T> find(final @NotNull String name) {
        final @Nullable StoredValue stored = this.argumentStore.get(name);
        return (Optional<T>) Optional.ofNullable(stored)
                .filter(StoredValue::isSet)
                .map(StoredValue::value);
    }

    public <T> @NotNull T findUnchecked(final @NotNull String name) {
        return (T) find(name).orElseThrow(() -> new NoSuchElementException(format("Could not find argument with name '%s'", name)));
    }

    public <T> @NotNull Optional<T> findAt(final int index) {
        final @Nullable String name = this.indexStore.get(index);
        if (name == null) {
            return Optional.empty();
        }

        final @Nullable StoredValue stored = this.argumentStore.get(name);
        return (Optional<T>) Optional.ofNullable(stored)
                .filter(StoredValue::isSet)
                .map(StoredValue::value);
    }

    public <T> @NotNull T findAtUnchecked(final int index) {
        return (T) findAt(index).orElseThrow(() -> new NoSuchElementException(format("Could not find argument at index %s", index)));
    }

    @Override
    public String toString() {
        return "CommandContext[" +
                "source=" + this.source + '\'' +
                ", commandLine='" + this.commandLine + '\'' +
                ", arguments=" + this.argumentStore +
                ", suggestions=" + this.suggestions +
                ']';
    }

    private static final record StoredValue(@Nullable Object value, boolean isSet) {}
}
