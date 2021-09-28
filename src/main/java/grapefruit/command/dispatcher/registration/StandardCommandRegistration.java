package grapefruit.command.dispatcher.registration;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public record StandardCommandRegistration<S>(@NotNull CommandContainer holder,
                                             @NotNull Method method,
                                             @NotNull List<CommandParameter<S>> parameters,
                                             @Nullable String perm,
                                             @Nullable TypeToken<?> sourceType,
                                             boolean runAsync) implements CommandRegistration<S> {
    @Override
    public @NotNull Optional<String> permission() {
        return Optional.ofNullable(perm());
    }

    @Override
    public @NotNull Optional<TypeToken<?>> commandSourceType() {
        return Optional.ofNullable(sourceType());
    }
}
