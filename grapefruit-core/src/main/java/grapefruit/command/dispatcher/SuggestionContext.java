package grapefruit.command.dispatcher;

import grapefruit.command.argument.CommandArgument;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Stores data gathered through the argument processing
 * stage, which is then used by {@link SuggestionsHelper}.
 */
public class SuggestionContext {
    /* Last user input part that wasn't fully consumed. */
    private @Nullable String lastInput;
    /* Last command argument that couldn't be processed */
    private @Nullable CommandArgument<?> lastArgument;
    /*
     * Stores whether the name of the flag - that was being
     * processed - was consumed successfully (meaning that
     * it was found to be valid both in format and in that
     * a flag argument with that name was found). If this is
     * true, and we're dealing with a value flag, the
     * corresponding argument mapper will provide suggestions.
     */
    private boolean suggestFlagValue;

    /**
     * @return The last part of the input, if any.
     */
    public Optional<String> lastInput() {
        return Optional.ofNullable(this.lastInput);
    }

    /**
     * @param input The new input
     * @return this
     */
    public SuggestionContext lastInput(@Nullable String input) {
        this.lastInput = input;
        return this;
    }

    /**
     * @return The last argument that was being processed,
     * if any
     */
    public Optional<CommandArgument<?>> lastArgument() {
        return Optional.ofNullable(this.lastArgument);
    }

    /**
     * @param argument The new argument
     * @return this
     */
    public SuggestionContext lastArgument(CommandArgument<?> argument) {
        this.lastArgument = argument;
        return this;
    }

    /**
     * @return Whether the flag name was fully consumed.
     */
    public boolean suggestFlagValue() {
        return this.suggestFlagValue;
    }

    /**
     * @param suggestFlagValue The new value
     * @return this
     */
    public SuggestionContext suggestFlagValue(boolean suggestFlagValue) {
        this.suggestFlagValue = suggestFlagValue;
        return this;
    }
}
