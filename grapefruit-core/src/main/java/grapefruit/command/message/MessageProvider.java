package grapefruit.command.message;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface MessageProvider {

    @NotNull String provide(final @NotNull MessageKey key);
}
