package grapefruit.command.parameter;

import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StandardParameter<S> extends AbstractCommandParameter<S> {

    public StandardParameter(final @NotNull String name,
                                final int index,
                                final boolean optional,
                                final @NotNull TypeToken<?> type,
                                final @NotNull AnnotationList modifiers,
                                final @NotNull ParameterMapper<S, ?> mapper) {
        super(name, index, optional, type, modifiers, mapper);
    }

    @Override
    public final boolean isFlag() {
        return false;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StandardParameter<?> that = (StandardParameter<?>) o;
        return this.index() == that.index()
                && this.isOptional() == that.isOptional()
                && Objects.equals(this.name(), that.name())
                && Objects.equals(this.type(), that.type())
                && Objects.equals(this.modifiers(), that.modifiers())
                && Objects.equals(this.mapper(), that.mapper());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name(), index(), isOptional(), type(), modifiers(), mapper());
    }

    @Override
    public String toString() {
        return "StandardParameter[" +
                "name='" + name() + '\'' +
                ", index=" + index() +
                ", optional=" + isOptional() +
                ", type=" + type() +
                ", modifiers=" + modifiers() +
                ", mapper=" + mapper().getClass().getName() +
                ']';
    }
}
