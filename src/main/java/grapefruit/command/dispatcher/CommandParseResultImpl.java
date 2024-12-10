package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.dispatcher.input.CommandInputTokenizer;
import grapefruit.command.util.ToStringer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CommandParseResultImpl<S> implements CommandParseResult<S> {
    private final @Nullable String input;
    private final @Nullable CommandArgument.Dynamic<S, ?> argument;
    private final @Nullable CommandException ex;
    private final List<CommandArgument.Required<S, ?>> arguments;
    private final List<CommandArgument.Flag<S, ?>> flags;
    private final int cursor;
    private final boolean flagNameConsumed;

    private CommandParseResultImpl(
            final @Nullable String input,
            final @Nullable CommandArgument.Dynamic<S, ?> argument,
            final @Nullable CommandException ex,
            final List<CommandArgument.Required<S, ?>> arguments,
            final List<CommandArgument.Flag<S, ?>> flags,
            final int cursor,
            final boolean flagNameConsumed
    ) {
        this.input = input;
        this.argument = argument;
        this.ex = ex;
        this.arguments = requireNonNull(arguments, "arguments cannot be null");
        this.flags = requireNonNull(flags, "flags cannot be null");
        this.cursor = cursor;
        this.flagNameConsumed = flagNameConsumed;
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
    public CommandParseResult<S> withInput(final String input) {
        return new CommandParseResultImpl<>(this.input, this.argument, this.ex, this.arguments, this.flags, this.cursor, this.flagNameConsumed);
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
    public int cursor() {
        return this.cursor;
    }

    @Override
    public boolean flagNameConsumed() {
        return this.flagNameConsumed;
    }

    @Override
    public String toString() {
        return ToStringer.create(this)
                .append("input", this.input)
                .append("argument", this.argument)
                .append("arguments", this.arguments)
                .append("flags", this.flags)
                .toString();
    }

    static final class Builder<S> implements CommandParseResult.Builder<S> {
        private final List<CommandArgument.Required<S, ?>> arguments;
        private final List<CommandArgument.Flag<S, ?>> flags;
        private final CommandInputTokenizer inputTokenizer;
        private CommandArgument.Dynamic<S, ?> argument;
        private String input;
        private CommandException capturedException;
        private int cursor;
        private boolean flagNameConsumed;

        Builder(final List<CommandArgument.Required<S, ?>> arguments, final List<CommandArgument.Flag<S, ?>> flags, final CommandInputTokenizer inputTokenizer) {
            this.arguments = requireNonNull(arguments, "arguments cannot be null");
            this.flags = requireNonNull(flags, "flags cannot be null");
            this.inputTokenizer = requireNonNull(inputTokenizer, "inputTokenizer cannot be null");
            this.cursor = inputTokenizer.cursor();
        }

        @Override
        public void begin(final CommandArgument.Dynamic<S, ?> argument, final String value) {
            requireNonNull(argument, "argument cannot be null");
            requireNonNull(value, "input cannot be null");
            this.flagNameConsumed = true;
            this.argument = argument;
            this.input = value;
            this.cursor = this.inputTokenizer.cursor();
        }

        @Override
        public void end() {
            if (this.argument != null) (this.argument.isFlag() ? this.flags : this.arguments).remove(this.argument);
            this.argument = null;
            this.input = null;
            this.flagNameConsumed = false;
        }

        @Override
        public void capture(final CommandException ex) {
            this.capturedException = requireNonNull(ex, "ex cannot be null");
        }

        @Override
        public CommandParseResult<S> build() {
            return new CommandParseResultImpl<>(this.input, this.argument, this.capturedException, this.arguments, this.flags, this.cursor, this.flagNameConsumed);
        }
    }
}
