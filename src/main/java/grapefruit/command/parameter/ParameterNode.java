package grapefruit.command.parameter;

import grapefruit.command.parameter.resolver.ParameterResolver;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public interface ParameterNode<S> {
    Pattern FLAG_PATTERN = Pattern.compile("^--(.+)$");

    @NotNull String name();

    @NotNull ParameterResolver<S, ?> resolver();

    @NotNull CommandParameter unwrap();
}
