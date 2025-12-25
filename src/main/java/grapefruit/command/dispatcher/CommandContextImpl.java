package grapefruit.command.dispatcher;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.util.ToStringer;
import grapefruit.command.util.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Objects.requireNonNull;

final class CommandContextImpl<S> implements CommandContext<S> {
    private final Map<Key<?>, Object> internalStore = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final S source;
    private final CommandChain<S> chain;

    CommandContextImpl(final S source, final CommandChain<S> chain) {
        this.source = requireNonNull(source, "source cannot be null");
        this.chain = requireNonNull(chain, "chain cannot be null");
    }

    @Override
    public S source() {
        return this.source;
    }

    @Override
    public CommandChain<S> chain() {
        return this.chain;
    }

    @Override
    public <T> Optional<T> get(final Key<T> key) {
        return Optional.ofNullable(nullable(key));
    }

    @Override
    public <T> T getOrDefault(final Key<T> key, final T fallback) {
        return get(key).orElse(fallback);
    }

    @Override
    public <T> T require(final Key<T> key) {
        return get(key).orElseThrow(() -> new NoSuchElementException("Could not find element in current context mapped to key '%s'".formatted(key)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T nullable(final Key<T> key) {
        try {
            this.lock.readLock().lock();
            Object found = this.internalStore.get(key);

            // TODO check for class cast errors
            return (T) found;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public boolean has(final Key<?> key) {
        try {
            this.lock.readLock().lock();
            return this.internalStore.containsKey(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public <T> void store(final Key<T> key, final T value) {
        internalStore(key, value, false);
    }

    @Override
    public <T> boolean replace(final Key<T> key, final T value) {
        return internalStore(key, value, true);
    }

    private <T> boolean internalStore(final Key<T> key, final T value, final boolean replace) {
        try {
            this.lock.writeLock().lock();
            if (has(key) && !replace) {
                throw new IllegalStateException("Cannot replace value mapped to key '%s'".formatted(key));
            }

            return this.internalStore.put(key, value) != null;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(final Key<?> key) {
        try {
            this.lock.writeLock().lock();
            return this.internalStore.remove(key) != null;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public Map<Key<?>, Object> asMap() {
        return Map.copyOf(this.internalStore);
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("internalStore", this.internalStore)
                .toString();
    }
}
