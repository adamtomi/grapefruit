package grapefruit.command.bungeecord;

import grapefruit.command.message.MessageKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BungeeMessageKeys {
    private static final Set<MessageKey> values = new HashSet<>();

    public static final MessageKey INVALID_PLAYER_VALUE = register(MessageKey.of("parameter.invalid-player-value"));

    private static @NotNull MessageKey register(final @NotNull MessageKey key) {
        values.add(key);
        return key;
    }

    public static @NotNull Set<MessageKey> values() {
        return Set.copyOf(values);
    }
}
