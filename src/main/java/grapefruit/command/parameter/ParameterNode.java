package grapefruit.command.parameter;

import grapefruit.command.parameter.resolver.ParameterResolver;
import org.jetbrains.annotations.NotNull;

public interface ParameterNode<S> {

    @NotNull String name();

    @NotNull ParameterResolver<S, ?> resolver();

    @NotNull CommandParameter unwrap();
}
