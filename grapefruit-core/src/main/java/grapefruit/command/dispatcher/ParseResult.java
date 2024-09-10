package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;

import java.util.List;
import java.util.Optional;

/**
 * Describes the result of the parsing of a command.
 */
public class ParseResult {

    public Optional<String> lastInput() {
        return Optional.empty();
    }

    public Optional<CommandArgument<?>> lastUnsuccessfulArgument() {
        return Optional.empty();
    }

    public Optional<CommandException> capturedException() {
        return Optional.empty();
    }

    // TODO see if we can get rid of this
    public boolean suggestFlagValue() {
        return false;
    }

    public boolean fullyConsumed() {
        return false;
    }

    public List<CommandArgument<?>> remaining() {
        return List.of();
    }

    public List<FlagArgument<?>> remainingFlags() {
        return List.of();
    }

    public Optional<String> remainingInput() {
        return Optional.empty();
    }

    public static class Builder {

        public Builder consuming(CommandArgument<?> argument) {
            return this;
        }

        public Builder consumed() {
            return this;
        }

        public ParseResult build() {
            return new ParseResult();
        }
    }
}
