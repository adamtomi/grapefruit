package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information about the command currently being parsed.
 */
final class CommandParseResult {
    /* Last user input part that wasn't fully consumed. */
    @Nullable String input;
    /* Last command argument that couldn't be processed */
    @Nullable CommandArgument<?> argument;
    /*
     * Stores whether the name of the flag - that was being
     * processed - was consumed successfully (meaning that
     * it was found to be valid both in format and in that
     * a flag argument with that name was found). If this is
     * true, and we're dealing with a value flag, the
     * corresponding argument mapper will provide suggestions.
     */
    boolean suggestFlagValue;
    /* Exception causing the parse process to fail */
    @Nullable CommandException capturedException;

    CommandParseResult() {}

    void reset() {
        this.input = null;
        this.argument = null;
        this.suggestFlagValue = false;
    }
}
