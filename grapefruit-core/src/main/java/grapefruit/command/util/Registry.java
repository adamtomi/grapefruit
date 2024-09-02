package grapefruit.command.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface Registry<K, V> {

    void store(K key, V value);

    Optional<V> get(K key);

    boolean has(K key);

    Map<K, V> asImmutableMap();

    void merge(Registry<K, V> other);

    static <K, V> Registry<K, V> create() {
        return new Impl<>();
    }

    final class Impl<K, V> implements Registry<K, V> {
        private final Map<K, V> internalMap = new HashMap<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        private Impl() {}

        @Override
        public void store(K key, V value) {
            try {
                this.lock.writeLock().lock();
                if (this.internalMap.containsKey(key)) {
                    throw new IllegalStateException("A value is already mapped to this key");
                }

                this.internalMap.put(key, value);
            } finally {
                this.lock.writeLock().unlock();
            }
        }

        @Override
        public Optional<V> get(K key) {
            try {
                this.lock.readLock().lock();
                return Optional.ofNullable(this.internalMap.get(key));
            } finally {
                this.lock.readLock().unlock();
            }
        }

        @Override
        public boolean has(K key) {
            try {
                this.lock.readLock().lock();
                return this.internalMap.containsKey(key);
            } finally {
                this.lock.readLock().unlock();
            }
        }

        @Override
        public Map<K, V> asImmutableMap() {
            try {
                this.lock.readLock().lock();
                return Map.copyOf(this.internalMap);
            } finally {
                this.lock.readLock().unlock();
            }
        }

        @Override
        public void merge(Registry<K, V> other) {
            try {
                this.lock.writeLock().lock();
                this.internalMap.putAll(other.asImmutableMap());
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }
}
