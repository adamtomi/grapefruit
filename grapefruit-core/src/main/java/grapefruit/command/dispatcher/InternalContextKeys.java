package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.util.key.Key;

public final class InternalContextKeys {
    public static final Key<Command> COMMAND = Key.named(Command.class, "grapefruit:internal:command");

    private InternalContextKeys() {}
}
