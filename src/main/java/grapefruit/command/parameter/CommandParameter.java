package grapefruit.command.parameter;

import com.google.common.reflect.TypeToken;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;

public interface CommandParameter<S> {

    @NotNull String name();

    boolean isFlag();

    boolean isOptional();

    @NotNull TypeToken<?> type();

    @NotNull AnnotationList modifiers();

    @NotNull ParameterMapper<S, ?> mapper();
}
