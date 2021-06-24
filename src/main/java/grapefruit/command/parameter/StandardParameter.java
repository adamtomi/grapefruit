package grapefruit.command.parameter;

import grapefruit.command.dispatcher.CommandArg;
import grapefruit.command.parameter.resolver.ParameterResolver;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Queue;

import static java.util.Objects.requireNonNull;

public class StandardParameter<S> implements ParameterNode<S> {
    private final String name;
    private final ParameterResolver<S, ?> resolver;
    private final CommandParameter param;

    public StandardParameter(final @NotNull String name,
                             final @NotNull ParameterResolver<S, ?> resolver,
                             final @NotNull CommandParameter param) {
        this.name = requireNonNull(name, "name cannot be null");
        this.resolver = requireNonNull(resolver, "resolver cannot be null");
        this.param = requireNonNull(param, "param cannot be null");
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull ParameterResolver<S, ?> resolver() {
        return this.resolver;
    }

    @Override
    public @NotNull CommandParameter unwrap() {
        return this.param;
    }

    @Override
    public String toString() {
        return "StandardParameter[" +
                "name='" + this.name + '\'' +
                ", resolver=" + this.resolver +
                ", param=" + this.param +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StandardParameter<?> that = (StandardParameter<?>) o;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.resolver, that.resolver)
                && Objects.equals(this.param, that.param);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.resolver, this.param);
    }

    public static final class PresenceFlag<S> extends StandardParameter<S> {
        public PresenceFlag(final @NotNull String name,
                            final @NotNull CommandParameter param) {
            super(name, new PresenceFlagResolver<>(name), param);
        }

        @Override
        public String toString() {
            return "PresenceFlag[" +
                    "name='" + name() + '\'' +
                    ", param=" + unwrap() +
                    ']';
        }

        private static final class PresenceFlagResolver<S> implements ParameterResolver<S, Void> {
            private final String name;

            private PresenceFlagResolver(final @NotNull String name) {
                this.name = requireNonNull(name, "name cannot be null");
            }

            @Override
            public final @NotNull TypeToken<Void> type() {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull Void resolve(final @NotNull S source,
                                         final @NotNull Queue<CommandArg> args,
                                         final @NotNull CommandParameter param) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                         final @NotNull String currentArg,
                                                         final @NotNull CommandParameter param) {
                return List.of(this.name);
            }
        }
    }

    public static final class ValueFlag<S> extends StandardParameter<S> {
        public ValueFlag(final @NotNull String name,
                         final @NotNull ParameterResolver<S, ?> resolver,
                         final @NotNull CommandParameter param) {
            super(name, resolver, param);
        }

        @Override
        public String toString() {
            return "ValueFlag[" +
                    "name='" + name() + '\'' +
                    ", resolver=" + resolver() +
                    ", param=" + unwrap() +
                    ']';
        }
    }
}
