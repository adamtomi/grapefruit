package grapefruit.command.dispatcher;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.util.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public interface CommandContext<S> {

    S source();

    CommandChain<S> chain();

    <T> Optional<T> get(final Key<T> key);

    <T> T getOrDefault(final Key<T> key, final T fallback);

    <T> T require(final Key<T> key);

    <T> @Nullable T nullable(final Key<T> key);

    boolean has(final Key<?> key);

    <T> void store(final Key<T> key, T value);

    <T> boolean replace(final Key<T> key, T value);

    boolean remove(final Key<?> key);

    Map<Key<?>, Object> asMap();
}
