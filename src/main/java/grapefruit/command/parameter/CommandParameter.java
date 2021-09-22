package grapefruit.command.parameter;

import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

public interface CommandParameter<S> {

    @NotNull String name();

    int index();

    boolean isFlag();

    boolean isOptional();

    @NotNull TypeToken<?> type();

    @NotNull AnnotationList modifiers();

    @NotNull ParameterMapper<S, ?> mapper();
}
