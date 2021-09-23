package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

public interface CommandInput {

    @NotNull String rawArg();

    boolean isConsumed();

    void markConsumed();
}
