package grapefruit.command.dispatcher.registration;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.condition.CommandCondition;
import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public record StandardCommandRegistration<S>(@NotNull CommandContainer container,
                                             @NotNull Method method,
                                             @NotNull List<CommandParameter<S>> parameters,
                                             @Nullable String perm,
                                             @Nullable CommandCondition<S> cmdCondition,
                                             @Nullable TypeToken<?> sourceType,
                                             boolean requiresContext,
                                             boolean runAsync) implements CommandRegistration<S> {
    @Override
    public @NotNull Optional<String> permission() {
        return Optional.ofNullable(perm());
    }

    @Override
    public @NotNull Optional<CommandCondition<S>> condition() {
        return Optional.ofNullable(cmdCondition());
    }

    @Override
    public @NotNull Optional<TypeToken<?>> commandSourceType() {
        return Optional.ofNullable(sourceType());
    }
}
