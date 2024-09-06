package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.dispatcher.input.StringReader;
import grapefruit.command.util.key.Key;

/**
 * This class stores keys that are required by
 * grapefruit internals to work properly.
 */
public final class InternalContextKeys {
    /* Key used to access the current command being executed */
    public static final Key<Command> COMMAND = Key.named(Command.class, "grapefruit:internal:command");
    /* Key used to access the reader wrapping the current user input */
    public static final Key<StringReader> INPUT = Key.named(StringReader.class, "grapefruit:internal:input");
    /* Key used to access parsing data related to the current command */
    public static final Key<ParseInfo> PARSE_INFO = Key.named(ParseInfo.class, "grapefruit:internal:parse-info");

    private InternalContextKeys() {}
}
