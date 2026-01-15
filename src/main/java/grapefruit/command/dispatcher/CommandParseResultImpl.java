package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.util.ToStringer;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CommandParseResultImpl<S> implements CommandParseResult<S> {
    private final CommandArgument.@Nullable Dynamic<S, ?> argument;
    private final @Nullable CommandException ex;
    private final List<CommandArgument.Required<S, ?>> arguments;
    private final List<CommandArgument.Flag<S, ?>> flags;

    private CommandParseResultImpl(
            final CommandArgument.@Nullable Dynamic<S, ?> argument,
            final @Nullable CommandException ex,
            final List<CommandArgument.Required<S, ?>> arguments,
            final List<CommandArgument.Flag<S, ?>> flags
    ) {
        this.argument = argument;
        this.ex = ex;
        this.arguments = requireNonNull(arguments, "arguments cannot be null");
        this.flags = requireNonNull(flags, "flags cannot be null");
    }

    @Override
    public void throwCaptured() throws CommandException {
        if (this.ex != null) throw this.ex;
    }

    @Override
    public <X extends CommandException> Optional<X> captured(final Class<X> clazz) {
        requireNonNull(clazz, "clazz cannot be null");
        return clazz.isInstance(this.ex)
                ? Optional.of(clazz.cast(this.ex))
                : Optional.empty();
    }

    @Override
    public Optional<CommandArgument.Dynamic<S, ?>> lastArgument() {
        return Optional.ofNullable(this.argument);
    }

    @Override
    public List<CommandArgument.Required<S, ?>> remainingArguments() {
        return List.copyOf(this.arguments);
    }

    @Override
    public List<CommandArgument.Flag<S, ?>> remainingFlags() {
        return List.copyOf(this.flags);
    }

    @Override
    public boolean isComplete() {
        return this.arguments.isEmpty() && this.flags.isEmpty();
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("lastArgument", this.argument)
                .append("remainingArguments", this.arguments)
                .append("remainingFlags", this.flags)
                .toString();
    }

    static final class Builder<S> implements CommandParseResult.Builder<S> {
        private final List<CommandArgument.Required<S, ?>> arguments;
        private final List<CommandArgument.Flag<S, ?>> flags;
        private CommandArgument.Dynamic<S, ?> argument;
        private CommandException capturedException;

        Builder(final List<CommandArgument.Required<S, ?>> arguments, final List<CommandArgument.Flag<S, ?>> flags) {
            this.arguments = requireNonNull(arguments, "arguments cannot be null");
            this.flags = requireNonNull(flags, "flags cannot be null");
        }

        @Override
        public void begin(final CommandArgument.Dynamic<S, ?> argument) {
            requireNonNull(argument, "argument cannot be null");
            this.argument = argument;
        }

        @Override
        public void end() {
            if (this.argument != null) (this.argument.isFlag() ? this.flags : this.arguments).remove(this.argument);
            this.argument = null;
        }

        @Override
        public void capture(final CommandException ex) {
            this.capturedException = requireNonNull(ex, "ex cannot be null");
        }

        @Override
        public CommandParseResult<S> build() {
            return new CommandParseResultImpl<>(this.argument, this.capturedException, this.arguments, this.flags);
        }
    }
}
