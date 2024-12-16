package grapefruit.command.mock;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.util.key.Key;

import java.util.Map;
import java.util.Optional;

public class NilCommandContext implements CommandContext<Object> {
    private final Object source = new Object();

    @Override
    public Object source() {
        return this.source;
    }

    @Override
    public CommandChain<Object> chain() {
        return null;
    }

    @Override
    public <T> Optional<T> get(Key<T> key) {
        return Optional.empty();
    }

    @Override
    public <T> T getOrDefault(Key<T> key, T fallback) {
        return null;
    }

    @Override
    public <T> T require(Key<T> key) {
        return null;
    }

    @Override
    public <T> T nullable(Key<T> key) {
        return null;
    }

    @Override
    public boolean has(Key<?> key) {
        return false;
    }

    @Override
    public <T> void store(Key<T> key, T value) {

    }

    @Override
    public <T> boolean replace(Key<T> key, T value) {
        return false;
    }

    @Override
    public boolean remove(Key<?> key) {
        return false;
    }

    @Override
    public Map<Key<?>, Object> asMap() {
        return Map.of();
    }
}
