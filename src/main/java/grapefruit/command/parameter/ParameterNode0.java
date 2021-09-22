package grapefruit.command.parameter;

import grapefruit.command.parameter.mapper.ParameterMapper;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@Deprecated
public interface ParameterNode0<S> {
    Pattern FLAG_PATTERN = Pattern.compile("^--(.+)$");

    @NotNull String name();

    @NotNull ParameterMapper<S, ?> mapper();

    @NotNull CommandParameter0 unwrap();
}
