package grapefruit.command.dispatcher.registration;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.condition.CommandCondition;
import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public interface CommandRegistration<S> {

    @NotNull CommandContainer container();

    @NotNull Method method();

    @NotNull List<CommandParameter<S>> parameters();

    @NotNull Optional<String> permission();

    @NotNull Optional<CommandCondition<S>> condition();

    @NotNull Optional<TypeToken<?>> commandSourceType();

    boolean requiresContext();

    boolean runAsync();
}
