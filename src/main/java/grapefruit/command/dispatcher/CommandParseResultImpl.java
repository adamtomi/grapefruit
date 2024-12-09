package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.util.ToStringer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CommandParseResultImpl<S> implements CommandParseResult<S> {
    private @Nullable String input;
    private final @Nullable CommandArgument.Dynamic<S, ?> argument;
    private final @Nullable CommandException ex;
    private final List<CommandArgument.Required<S, ?>> arguments;
    private final List<CommandArgument.Flag<S, ?>> flags;

    private CommandParseResultImpl(
            final @Nullable String input,
            final @Nullable CommandArgument.Dynamic<S, ?> argument,
            final @Nullable CommandException ex,
            final List<CommandArgument.Required<S, ?>> arguments,
            final List<CommandArgument.Flag<S, ?>> flags
    ) {
        this.input = input;
        this.argument = argument;
        this.ex = ex;
        this.arguments = requireNonNull(arguments, "arguments cannot be null");
        this.flags = requireNonNull(flags, "flags cannot be null");
    }

    @Override
    public Optional<CommandException> capturedException() {
        return Optional.ofNullable(this.ex);
    }

    @Override
    public void rethrowCaptured() throws CommandException {
        if (this.ex != null) throw this.ex;
    }

    @Override
    public Optional<String> lastInput() {
        return Optional.ofNullable(this.input);
    }

    @Override
    public void setLastInput(final String lastInput) {
        this.input = lastInput;
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
    public String toString() {
        return ToStringer.create(this)
                .append("input", this.input)
                .append("argument", this.argument)
                .append("remainingArguments", this.arguments)
                .append("flags", this.flags)
                .toString();
    }

    static final class Builder<S> implements CommandParseResult.Builder<S> {
        private final List<CommandArgument.Required<S, ?>> arguments;
        private final List<CommandArgument.Flag<S, ?>> flags;
        private CommandArgument.Dynamic<S, ?> argument;
        private String input;
        private CommandException capturedException;

        Builder(final List<CommandArgument.Required<S, ?>> arguments, final List<CommandArgument.Flag<S, ?>> flags) {
            this.arguments = requireNonNull(arguments, "arguments cannot be null");
            this.flags = requireNonNull(flags, "flags cannot be null");
        }

        @Override
        public void begin(final CommandArgument.Dynamic<S, ?> argument, final String input) {
            requireNonNull(argument, "argument cannot be null");
            requireNonNull(input, "input cannot be null");
            (argument.isFlag() ? this.flags : this.arguments).remove(argument);
            this.argument = argument;
            this.input = input;
        }

        @Override
        public void end() {
            this.argument = null;
            this.input = null;
        }

        @Override
        public void capture(final CommandException ex) {
            this.capturedException = requireNonNull(ex, "ex cannot be null");
        }

        @Override
        public CommandParseResult<S> build() {
            return new CommandParseResultImpl<>(this.input, this.argument, this.capturedException, this.arguments, this.flags);
        }
    }
}
