package grapefruit.command.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Objects.requireNonNull;

public interface Registry<K, V> {

    void store(K key, V value);

    Optional<V> get(K key);

    boolean has(K key);

    V remove(K key);

    Map<K, V> asImmutableMap();

    void merge(Registry<K, V> other);

    static <K, V> Registry<K, V> create(DuplicateStrategy<V> duplicateStrategy) {
        return new Impl<>(duplicateStrategy);
    }

    final class Impl<K, V> implements Registry<K, V> {
        private final Map<K, V> internalMap = new HashMap<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private final DuplicateStrategy<V> duplicateStrategy;

        private Impl(DuplicateStrategy<V> duplicateStrategy) {
            this.duplicateStrategy = requireNonNull(duplicateStrategy, "duplcateStrategy cannot be null");
        }

        @Override
        public void store(K key, V value) {
            try {
                this.lock.writeLock().lock();
                V existing = this.internalMap.get(key);
                V newValue = existing != null
                        ? this.duplicateStrategy.handle(existing, value)
                        : value;

                this.internalMap.put(key, newValue);
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
        public V remove(K key) {
            try {
                this.lock.writeLock().lock();
                return this.internalMap.remove(key);
            } finally {
                this.lock.writeLock().unlock();
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

    /**
     * Handles the insertion of duplicate entries.
     *
     * @param <T> The value type of the registy
     */
    @FunctionalInterface
    interface DuplicateStrategy<T> {

        /**
         * Determines which value to keep, if any.
         *
         * @param oldValue The old value
         * @param newValue The new value
         * @return The value to store
         */
        T handle(T oldValue, T newValue);

        /**
         * Creates a duplicate strategy that always
         * replaces the old stored value with the
         * new one.
         *
         * @param <T> The value type of the registry
         * @return The created duplicate handler
         */
        static <T> DuplicateStrategy<T> replace() {
            return (oldValue, newValue) -> newValue;
        }

        /**
         * Creates a duplicate strategy that always
         * throws an error if duplicate entries
         * are attempted to be inserted into the
         * registry.
         *
         * @param <T> The value type of the registry
         * @return The created duplicate handler
         */
        static <T> DuplicateStrategy<T> reject() {
            return (oldValue, newValue) -> {
                throw new UnsupportedOperationException("This registry does not support the replacement of values.");
            };
        }
    }
}
