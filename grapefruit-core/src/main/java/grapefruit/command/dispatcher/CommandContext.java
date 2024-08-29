package grapefruit.command.dispatcher;

import grapefruit.command.util.key.Key;

import java.util.Optional;

public class CommandContext {

    // TODO
    public <T> Optional<T> getSafe(Key<?> key) {
        return Optional.empty();
    }

    // TODO
    @SuppressWarnings("unchecked")
    public <T> T get(Key<?> key) {
        return (T) getSafe(key).orElseThrow();
    }
}
