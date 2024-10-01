package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.binding.BoundArgument;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Describes the result of the parsing of a command.
 */
class ParseResult {
    private final List<BoundArgument<?>> remaining;
    private final List<BoundArgument<?>> remainingFlags;
    @Nullable
    private final BoundArgument<?> lastUnsuccessfulArgument;
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

    public Optional<BoundArgument<?>> lastUnsuccessfulArgument() {
        return Optional.ofNullable(this.lastUnsuccessfulArgument);
    }

    public void rethrowCaptured() throws CommandException {
        if (this.capturedException != null) throw this.capturedException;
    }

    public boolean fullyConsumed() {
        return this.remaining.isEmpty() && this.remainingFlags.isEmpty();
    }

    public List<BoundArgument<?>> remaining() {
        return List.copyOf(this.remaining);
    }

    public List<BoundArgument<?>> remainingFlags() {
        return List.copyOf(this.remainingFlags);
    }

    @Override
    public String toString() {
        return "ParseResult(lastUnsuccessfulArgument=%s, lastInput=%s, capturedException=%s)".formatted(
                this.lastUnsuccessfulArgument, this.lastInput, this.capturedException
        );
    }

    static Builder parsing(CommandInfo commandInfo) {
        requireNonNull(commandInfo, "commandInfo cannot be null");
        // Create mutable copies
        return new Builder(new ArrayList<>(commandInfo.arguments()), new ArrayList<>(commandInfo.flags()));
    }

    final static class Builder {
        private final List<BoundArgument<?>> required;
        private final List<BoundArgument<?>> flags;
        @Nullable
        private String currentInput;
        @Nullable
        private BoundArgument<?> currentArgument;
        @Nullable
        private CommandException capturedException;

        private Builder(List<BoundArgument<?>> required, List<BoundArgument<?>> flags) {
            this.required = requireNonNull(required, "required cannot be null");
            this.flags = requireNonNull(flags, "flags cannot be null");
        }

        /* Sets the argument currently being consumed */
        public Builder consuming(BoundArgument<?> argument) {
            // Remove the argument from the remaining (unseen) arguments
            (argument.argument().isFlag() ? this.flags : this.required).remove(argument);
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
