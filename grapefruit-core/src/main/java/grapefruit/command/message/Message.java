package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Message {

    @NotNull MessageKey key();

    @NotNull List<Template> templates();

    @NotNull String get(final @NotNull MessageProvider provider);

    static @NotNull Message of(final @NotNull MessageKey key,
                               final @NotNull Template... templates) {
        return new MessageImpl(key, List.of(templates));
    }
}
