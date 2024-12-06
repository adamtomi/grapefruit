package grapefruit.command.dispatcher;

import grapefruit.command.argument.CommandArgument;
import grapefruit.command.util.ToStringer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CommandParseResultImpl<S> implements CommandParseResult<S> {
    private final @Nullable String input;
    private final @Nullable CommandArgument.Dynamic<S, ?> argument;
    private final List<CommandArgument.Required<S, ?>> arguments;
    private final List<CommandArgument.Flag<S, ?>> flags;

    private CommandParseResultImpl(
            final @Nullable String input,
            final @Nullable CommandArgument.Dynamic<S, ?> argument,
            final List<CommandArgument.Required<S, ?>> arguments,
            final List<CommandArgument.Flag<S, ?>> flags
    ) {
        this.input = input;
        this.argument = argument;
        this.arguments = requireNonNull(arguments, "arguments cannot be null");
        this.flags = requireNonNull(flags, "flags cannot be null");
    }

    @Override
    public Optional<String> lastInput() {
        return Optional.ofNullable(this.input);
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
        public CommandParseResult<S> build() {
            return new CommandParseResultImpl<>(this.input, this.argument, this.arguments, this.flags);
        }
    }
}
