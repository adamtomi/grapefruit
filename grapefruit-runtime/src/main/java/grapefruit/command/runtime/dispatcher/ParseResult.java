package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.CommandException;
import grapefruit.command.runtime.argument.CommandArgument;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Describes the result of the parsing of a command.
 */
class ParseResult {
    private final List<CommandArgument<?>> remaining;
    private final List<CommandArgument.Flag<?>> remainingFlags;
    @Nullable
    private final CommandArgument<?> lastUnsuccessfulArgument;
    @Nullable
    private final String lastInput;
    @Nullable
    private final CommandException capturedException;

    private ParseResult(Builder builder) {
        requireNonNull(builder, "builder cannot be null");
        this.remaining = builder.required;
        this.remainingFlags = builder.flags;
        this.lastUnsuccessfulArgument = builder.currentArgument;
        this.lastInput = builder.currentInput;
        this.capturedException = builder.capturedException;
    }


    public Optional<String> lastInput() {
        return Optional.ofNullable(this.lastInput);
    }

    public Optional<CommandArgument<?>> lastUnsuccessfulArgument() {
        return Optional.ofNullable(this.lastUnsuccessfulArgument);
    }

    public void rethrowCaptured() throws CommandException {
        if (this.capturedException != null) throw this.capturedException;
    }

    public boolean wasCaptured(Class<? extends CommandException> clazz) {
        return clazz.isInstance(this.capturedException);
    }

    public boolean fullyConsumed() {
        return this.remaining.isEmpty() && this.remainingFlags.isEmpty();
    }

    public List<CommandArgument<?>> remaining() {
        return List.copyOf(this.remaining);
    }

    public List<CommandArgument.Flag<?>> remainingFlags() {
        return List.copyOf(this.remainingFlags);
    }

    @Override
    public String toString() {
        return "ParseResult(lastUnsuccessfulArgument=%s, lastInput=%s, capturedException=%s)".formatted(
                this.lastUnsuccessfulArgument, this.lastInput, this.capturedException
        );
    }

    static Builder parsing(CommandDefinition command) {
        requireNonNull(command, "command cannot be null");
        // Create mutable copies
        return new Builder(new ArrayList<>(command.arguments()), new ArrayList<>(command.flags()));
    }

    final static class Builder {
        private final List<CommandArgument<?>> required;
        private final List<CommandArgument.Flag<?>> flags;
        @Nullable
        private String currentInput;
        @Nullable
        private CommandArgument<?> currentArgument;
        @Nullable
        private CommandException capturedException;

        private Builder(List<CommandArgument<?>> required, List<CommandArgument.Flag<?>> flags) {
            this.required = requireNonNull(required, "required cannot be null");
            this.flags = requireNonNull(flags, "flags cannot be null");
        }

        /* Sets the argument currently being consumed */
        public Builder consuming(CommandArgument<?> argument) {
            // Remove the argument from the remaining (unseen) arguments
            (argument.isFlag() ? this.flags : this.required).remove(argument);
            this.currentArgument = argument;
            return this;
        }

        public Builder consuming(String input) {
            this.currentInput = input;
            return this;
        }

        public Builder consumed() {
            this.currentArgument = null;
            this.currentInput = null;
            return this;
        }

        public Builder capture(CommandException ex) {
            this.capturedException = ex;
            return this;
        }

        public ParseResult build() {
            return new ParseResult(this);
        }
    }
}
