package grapefruit.command.dispatcher;

import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Stores data related to the current command execution. This data
 * mainly consists of parsed values of command arguments, but
 * any value can be stored with a {@link Key key} instance. Command
 * handler functions are able to retrieve these values.
 */
public class CommandContext {
    private final Registry<Key<?>, Object> internalStore = Registry.create();

    // TODO
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true)
    public <T> Optional<T> getSafe(Key<T> key) {
        return (Optional<T>) this.internalStore.get(key) ;
    }

    // TODO
    @Deprecated(forRemoval = true)
    public <T> T _get(Key<T> key) {
        return getSafe(key).orElseThrow();
    }

    public boolean has(Key<?> key) {
        return this.internalStore.has(key);
    }

    @Deprecated(forRemoval = true)
    public <T> void store(Key<T> key, T instance) {
        if (this.internalStore.has(key)) throw new IllegalStateException("Key '%s' is already stored in this context".formatted(key));
        this.internalStore.store(key, instance);
    }

    /**
     * Finds and returns the value associated with the
     * provided key, wrapped in an {@link Optional}. If
     * no value is mapped to the key, {@link Optional#empty()}
     * is returned.
     *
     * @param <T> The expected argument type
     * @param key The key
     * @return The found value, or an empty optional
     */
    public <T> Optional<T> get(Key<T> key) {
        requireNonNull(key, "key cannot be null");
        return Optional.ofNullable(nullable(key));
    }

    /**
     * Finds and returns the value associated with the
     * provided key, or throws an {@link IllegalArgumentException},
     * if nothing is mapped to the key.
     *
     * @param <T> The expected argument type
     * @param key The key
     * @return The found value
     * @throws IllegalArgumentException If nothing is mapped to the key
     */
    public <T> T need(Key<T> key) {
        requireNonNull(key, "key cannot be null");
        // TODO probably need to throw a different exception. Maybe
        return get(key).orElseThrow(() -> new IllegalArgumentException("Nothing is mapped to key '%s'".formatted(key)));
    }

    /**
     * Find and returns the value associated with the
     * provided key, or null if nothing is mapped to
     * the key.
     *
     * @param <T> The expected argument type
     * @param key The key
     * @return The value, or null
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T nullable(Key<T> key) {
        requireNonNull(key, "key cannot be null");
        return (T) this.internalStore.get(key);
    }

    // TODO add remove, contains, asMap() and such
}
