package grapefruit.command.parameter;

import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

public class ValueFlagParameter<S> extends AbstractFlagParameter<S> {

    public ValueFlagParameter(final @NotNull String flagName,
                                 final @NotNull String name,
                                 final int index,
                                 final @NotNull TypeToken<?> type,
                                 final @NotNull AnnotationList modifiers,
                                 final @NotNull ParameterMapper<S, ?> mapper) {
        super(flagName, name, index, true, type, modifiers, mapper);
    }

    @Override
    public @NotNull String toString() {
        return "ValueFlagParameter[" +
                "name='" + flagName() + '\'' +
                ", parameterName='" + name() + '\'' +
                ", index=" + index() +
                ", type=" + type() +
                ", modifiers=" + modifiers() +
                ", mapper=" + mapper().getClass().getName() +
                ']';
    }
}
