package grapefruit.command.dispatcher.registration;

import com.google.common.reflect.TypeToken;
import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public record CommandRegistration<S>(@NotNull Object holder,
                                     @NotNull Method method,
                                     @NotNull List<CommandParameter<S>> parameters,
                                     @Nullable String permission,
                                     @Nullable TypeToken<?> commandSourceType,
                                     boolean runAsync) {}
