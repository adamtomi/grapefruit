package grapefruit.command.parameter;

import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class StandardParameter<S> implements CommandParameter<S> {
    private final String name;
    private final int index;
    private final boolean optional;
    private final TypeToken<?> type;
    private final AnnotationList modifiers;
    private final ParameterMapper<S, ?> mapper;

    protected StandardParameter(final @NotNull String name,
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
    public final boolean isFlag() {
        return false;
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

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StandardParameter<?> that = (StandardParameter<?>) o;
        return this.index == that.index
                && this.optional == that.optional
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.modifiers, that.modifiers)
                && Objects.equals(this.mapper, that.mapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.index, this.optional, this.type, this.modifiers,this. mapper);
    }

    @Override
    public String toString() {
        return "StandardParameter[" +
                "name='" + this.name + '\'' +
                ", index=" + this.index +
                ", optional=" + this.optional +
                ", type=" + this.type +
                ", modifiers=" + this.modifiers +
                ", mapper=" + this.mapper +
                ']';
    }
}
