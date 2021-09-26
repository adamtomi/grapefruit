package grapefruit.command.parameter;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface FlagValueSet<T> {

    @NotNull TypeToken<T> type();

    @NotNull List<T> asList();

    @NotNull Set<T> asSet();

    <K> @NotNull Map<K, T> mapToKeys(final @NotNull Function<T, K> keyGenerator);

    <V> @NotNull Map<T, V> mapValues(final @NotNull Function<T, V> valueGeneator);
}
