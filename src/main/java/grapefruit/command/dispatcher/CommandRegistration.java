package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public record CommandRegistration(@NotNull MethodHandle methodHandle,
                                  @NotNull List<ParameterNode> parameters,
                                  @Nullable String permission,
                                  boolean requiresCommandSource,
                                  boolean runAsync) {
}
