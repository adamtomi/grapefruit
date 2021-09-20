package grapefruit.command.parameter;

import grapefruit.command.parameter.mapper.ParameterMapper;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public interface ParameterNode<S> {
    Pattern FLAG_PATTERN = Pattern.compile("^--(.+)$");

    @NotNull String name();

    @NotNull ParameterMapper<S, ?> mapper();

    @NotNull CommandParameter unwrap();
}
