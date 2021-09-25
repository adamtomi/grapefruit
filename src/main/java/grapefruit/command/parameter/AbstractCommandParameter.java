package grapefruit.command.parameter;

import com.google.common.reflect.TypeToken;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

abstract class AbstractCommandParameter<S> implements CommandParameter<S> {
    private final String name;
    private final int index;
    private final boolean optional;
    private final TypeToken<?> type;
    private final AnnotationList modifiers;
    private final ParameterMapper<S, ?> mapper;

    protected AbstractCommandParameter(final @NotNull String name,
                                final int index,
                                final boolean optional,
                                final @NotNull TypeToken<?> type,
                                final @NotNull AnnotationList modifiers,
                                final @NotNull ParameterMapper<S, ?> mapper) {
        this.name = requireNonNull(name, "name cannot be null");
        this.index = index;
        this.optional = optional;
        this.type = requireNonNull(type, "type cannot be null");
        this.modifiers = requireNonNull(modifiers, "modifiers cannot be null");
        this.mapper = requireNonNull(mapper, "mapper cannot be null");
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public int index() {
        return this.index;
    }

    @Override
    public boolean isOptional() {
        return this.optional;
    }

    @Override
    public @NotNull TypeToken<?> type() {
        return this.type;
    }

    @Override
    public @NotNull AnnotationList modifiers() {
        return this.modifiers;
    }

    @Override
    public @NotNull ParameterMapper<S, ?> mapper() {
        return this.mapper;
    }
}
