package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.binding.BoundArgument;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Stores parsing-related information such as the
 * argument currently being parsed, input currently
 * being confused about a command execution.
 */
public class ParseInfo {
    /* The input currently being consumed */
    private @Nullable String input;
    /* The argument that couldn't be parsed */
    private @Nullable BoundArgument<?> argument;
    /*
     * Stores whether the name of the flag - that was being
     * processed - was consumed successfully (meaning that
     * it was found to be valid both in format and in that
     * a flag argument with that name was found). If this is
     * true, and we're dealing with a value flag, the
     * corresponding argument mapper will provide suggestions.
     */
    private boolean suggestFlagValue;
    /* Exception causing the parse process to fail */
    private @Nullable CommandException capturedException;

    public void reset() {
        this.input = null;
        this.argument = null;
        this.suggestFlagValue = false;
    }

    public Optional<String> input() {
        return Optional.ofNullable(this.input);
    }

    public void input(String input) {
        this.input = input;
    }

    public Optional<BoundArgument<?>> argument() {
        return Optional.ofNullable(this.argument);
    }

    public void argument(BoundArgument<?> argument) {
        this.argument = argument;
    }

    public boolean suggestFlagValue() {
        return this.suggestFlagValue;
    }

    public void suggestFlagValue(boolean suggestFlagValue) {
        this.suggestFlagValue = suggestFlagValue;
    }

    public Optional<CommandException> capturedException() {
        return Optional.ofNullable(this.capturedException);
    }

    public void capturedException(CommandException capturedException) {
        this.capturedException = capturedException;
    }
}
