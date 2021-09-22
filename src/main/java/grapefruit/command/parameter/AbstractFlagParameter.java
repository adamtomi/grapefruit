package grapefruit.command.parameter;

import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class AbstractFlagParameter<S> extends AbstractCommandParameter<S> implements FlagParameter<S> {
    private final String flagName;

    public AbstractFlagParameter(final @NotNull String flagName,
                                 final @NotNull String name,
                                 final int index,
                                 final boolean optional,
                                 final @NotNull TypeToken<?> type,
                                 final @NotNull AnnotationList modifiers,
                                 final @NotNull ParameterMapper<S, ?> mapper) {
        super(name, index, optional, type, modifiers, mapper);
        this.flagName = requireNonNull(flagName, "flagName cannot be null");
    }

    @Override
    public @NotNull String flagName() {
        return this.flagName;
    }

    @Override
    public final boolean isFlag() {
        return true;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AbstractFlagParameter<?> that = (AbstractFlagParameter<?>) o;
        return this.index() == that.index()
                && this.isOptional() == that.isOptional()
                && Objects.equals(this.flagName(), that.flagName())
                && Objects.equals(this.name(), that.name())
                && Objects.equals(this.type(), that.type())
                && Objects.equals(this.modifiers(), that.modifiers())
                && Objects.equals(this.mapper(), that.mapper());
    }

    @Override
    public int hashCode() {
        return Objects.hash(flagName(), name(), index(), isOptional(), type(), modifiers(), mapper());
    }

    @Override
    public abstract @NotNull String toString();
}
