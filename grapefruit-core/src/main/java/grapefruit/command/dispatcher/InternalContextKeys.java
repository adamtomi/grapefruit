package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.util.key.Key;

/**
 * This class stores keys that are required by
 * grapefruit internals to work properly.
 */
public final class InternalContextKeys {
    public static final Key<Command> COMMAND = Key.named(Command.class, "grapefruit:internal:command");

    private InternalContextKeys() {}
}