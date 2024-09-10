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

    private InternalContextKeys() {}
}
