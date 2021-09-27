package grapefruit.command.parameter;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class FlagValueSetImpl<T> implements FlagValueSet<T> {
    private final TypeToken<T> type;
    private final List<T> elements;

    public FlagValueSetImpl(final @NotNull TypeToken<T> type, final @NotNull List<T> elements) {
        this.type = requireNonNull(type, "type cannot be null");
        this.elements = List.copyOf(requireNonNull(elements, "elements cannot be null"));
    }

    public void add(final @NotNull T element) {
        requireNonNull(element, "element cannot be null");
    }

    @Override
    public @NotNull TypeToken<T> type() {
        return this.type;
    }

    @Override
    public @NotNull List<T> asList() {
        return this.elements;
    }

    @Override
    public @NotNull Set<T> asSet() {
        return Set.copyOf(this.elements);
    }

    @Override
    public @NotNull <K> Map<K, T> mapToKeys(final @NotNull Function<T, K> keyGenerator) {
        requireNonNull(keyGenerator, "keyGenerator cannot be null");
        final Map<K, T> result = this.elements.stream()
                .collect(Collectors.toMap(keyGenerator, Function.identity()));
        return Map.copyOf(result);
    }

    @Override
    public @NotNull <V> Map<T, V> mapValues(final @NotNull Function<T, V> valueGeneator) {
        requireNonNull(valueGeneator, "valueGenerator cannot be null");
        final Map<T, V> result = this.elements.stream()
                .collect(Collectors.toMap(Function.identity(), valueGeneator));
        return Map.copyOf(result);
    }

    @Override
    public @NotNull String toString() {
        return "FlagValueSetImpl[" +
                "type=" + this.type +
                ", elements=" + this.elements +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FlagValueSetImpl<?> that = (FlagValueSetImpl<?>) o;
        return Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.elements);
    }
}
