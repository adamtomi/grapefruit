package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface MessageKey {

    @NotNull String key();

    static @NotNull MessageKey of(final @NotNull String key) {
        return new MessageKeyImpl(key);
    }
}
