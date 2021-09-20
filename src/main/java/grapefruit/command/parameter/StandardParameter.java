package grapefruit.command.parameter;

import grapefruit.command.dispatcher.CommandArgument;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.Miscellaneous;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Queue;

import static java.util.Objects.requireNonNull;

public class StandardParameter<S> implements ParameterNode<S> {
    private final String name;
    private final ParameterMapper<S, ?> mapper;
    private final CommandParameter param;

    public StandardParameter(final @NotNull String name,
                             final @NotNull ParameterMapper<S, ?> mapper,
                             final @NotNull CommandParameter param) {
        this.name = requireNonNull(name, "name cannot be null");
        this.mapper = requireNonNull(mapper, "mapper cannot be null");
        this.param = requireNonNull(param, "param cannot be null");
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull ParameterMapper<S, ?> mapper() {
        return this.mapper;
    }

    @Override
    public @NotNull CommandParameter unwrap() {
        return this.param;
    }

    @Override
    public String toString() {
        return "StandardParameter[" +
                "name='" + this.name + '\'' +
                ", mapper=" + this.mapper +
                ", param=" + this.param +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StandardParameter<?> that = (StandardParameter<?>) o;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.mapper, that.mapper)
                && Objects.equals(this.param, that.param);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.mapper, this.param);
    }

    public static final class PresenceFlag<S> extends StandardParameter<S> {
        public PresenceFlag(final @NotNull String name,
                            final @NotNull CommandParameter param) {
            super(name, new PresenceFlagMapper<>(name), param);
        }

        @Override
        public String toString() {
            return "PresenceFlag[" +
                    "name='" + name() + '\'' +
                    ", param=" + unwrap() +
                    ']';
        }

        private static final class PresenceFlagMapper<S> implements ParameterMapper<S, Void> {
            private final String name;

            private PresenceFlagMapper(final @NotNull String name) {
                this.name = requireNonNull(name, "name cannot be null");
            }

            @Override
            public @NotNull TypeToken<Void> type() {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull Void map(final @NotNull S source,
                                     final @NotNull Queue<CommandArgument> args,
                                     final @NotNull CommandParameter param) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                         final @NotNull String currentArg,
                                                         final @NotNull CommandParameter param) {
                return List.of(Miscellaneous.formatFlag(this.name));
            }

            @Override
            public boolean suggestionsNeedValidation() {
                return false;
            }
        }
    }

    public static final class ValueFlag<S> extends StandardParameter<S> {
        private final String parameterName;

        public ValueFlag(final @NotNull String name,
                         final @NotNull ParameterMapper<S, ?> mapper,
                         final @NotNull CommandParameter param,
                         final @NotNull String parameterName) {
            super(name, mapper, param);
            this.parameterName = requireNonNull(parameterName, "parameterName cannot be null");
        }

        public @NotNull String parameterName() {
            return this.parameterName;
        }

        @Override
        public String toString() {
            return "ValueFlag[" +
                    "name='" + name() + '\'' +
                    ", mapper=" + mapper() +
                    ", param=" + unwrap() +
                    ", parameterName='" + parameterName() + + '\'' +
                    ']';
        }
    }
}
