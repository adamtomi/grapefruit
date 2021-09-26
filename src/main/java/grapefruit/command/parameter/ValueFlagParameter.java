package grapefruit.command.parameter;

import com.google.common.reflect.TypeToken;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;

public class ValueFlagParameter<S> extends AbstractFlagParameter<S> {
    private final boolean multi;

    public ValueFlagParameter(final @NotNull String flagName,
                              final char shorthand,
                              final boolean multi,
                              final @NotNull String name,
                              final int index,
                              final @NotNull TypeToken<?> type,
                              final @NotNull AnnotationList modifiers,
                              final @NotNull ParameterMapper<S, ?> mapper) {
        super(flagName, shorthand, name, index, true, type, modifiers, mapper);
        this.multi = multi;
    }

    @Override
    public boolean isMultiFlag() {
        return this.multi;
    }

    @Override
    public @NotNull String toString() {
        return "ValueFlagParameter[" +
                "name='" + flagName() + '\'' +
                ", shorthand='" + shorthand() + '\'' +
                ", multi='" + isMultiFlag() + '\'' +
                ", parameterName='" + name() + '\'' +
                ", index=" + index() +
                ", type=" + type() +
                ", modifiers=" + modifiers() +
                ", mapper=" + mapper().getClass().getName() +
                ']';
    }
}
