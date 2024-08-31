package grapefruit.command.dispatcher;

import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;

import java.util.Optional;

public class CommandContext {
    private final Registry<Key<?>, Object> internalStore = Registry.create();

    // TODO
    public <T> Optional<T> getSafe(Key<?> key) {
        return Optional.empty();
    }

    // TODO
    @SuppressWarnings("unchecked")
    public <T> T get(Key<?> key) {
        return (T) getSafe(key).orElseThrow();
    }

    public <T> void store(Key<T> key, T instance) {
        if (this.internalStore.has(key)) throw new IllegalStateException("Key '%s' is already stored in this context".formatted(key));
        this.internalStore.store(key, instance);
    }
}
