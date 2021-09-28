package grapefruit.command.dispatcher.registration;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.parameter.CommandParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class RedirectingCommandRegistration<S> implements CommandRegistration<S> {
    private final CommandRegistration<S> delegate;

    public RedirectingCommandRegistration(final @NotNull CommandRegistration<S> delegate) {
        this.delegate = requireNonNull(delegate, "delegate cannot be null");
        if (!delegate.parameters().isEmpty()) {
            throw new IllegalStateException("Parameters must be empty");
        }
    }

    @Override
    public @NotNull CommandContainer holder() {
        return this.delegate.holder();
    }

    @Override
    public @NotNull Method method() {
        return this.delegate.method();
    }

    @Override
    public @NotNull List<CommandParameter<S>> parameters() {
        return this.delegate.parameters();
    }

    @Override
    public @NotNull Optional<String> permission() {
        return this.delegate.permission();
    }

    @Override
    public @NotNull Optional<TypeToken<?>> commandSourceType() {
        return this.delegate.commandSourceType();
    }

    @Override
    public boolean runAsync() {
        return this.delegate.runAsync();
    }

    @Override
    public @NotNull String toString() {
        return "RedirectingCommandRegistration[" +
                "delegate=" + this.delegate +
                ']';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RedirectingCommandRegistration<?> that = (RedirectingCommandRegistration<?>) o;
        return Objects.equals(this.delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.delegate);
    }
}
