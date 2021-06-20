package grapefruit.command.dispatcher;

import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.resolver.ParameterResolver;
import org.jetbrains.annotations.NotNull;

public record ParameterNode<S>(@NotNull ParameterResolver<S, ?> resolver, @NotNull CommandParameter parameter) {}
