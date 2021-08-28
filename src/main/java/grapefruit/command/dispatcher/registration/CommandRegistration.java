package grapefruit.command.dispatcher.registration;

import grapefruit.command.parameter.ParameterNode;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public record CommandRegistration<S>(@NotNull MethodHandle methodHandle,
                                     @NotNull List<ParameterNode<S>> parameters,
                                     @Nullable String permission,
                                     @Nullable TypeToken<?> commandSourceType,
                                     boolean requiresCommandSource,
                                     boolean runAsync) {
}
