package io.grapefruit.command.dispatcher;

import io.grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Set;

public record CommandRegistration(@NotNull MethodHandle methodHandle,
                                  @NotNull Set<CommandParameter> parameters,
                                  @Nullable String permission,
                                  boolean runSync) {
}
