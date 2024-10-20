package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.generated.CommandMirror;
import grapefruit.command.runtime.util.key.Key;

/**
 * This class stores keys that are required by
 * grapefruit internals to work properly.
 */
public final class InternalContextKeys {
    /* Key used to access the current command being executed */
    public static final Key<CommandMirror> COMMAND = Key.named(CommandMirror.class, "grapefruit:internal:command");
    /* Key used to access the wrapper command definition */
    public static final Key<CommandDefinition> COMMAND_DEF_KEY = Key.named(CommandDefinition.class, "grapefruit:internal:command-definition");

    private InternalContextKeys() {}
}
