package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.util.key.Key;

public final class StandardContextKeys {
    public static final Key<Command> COMMAND_INSTANCE = Key.named(Command.class, "__COMMAND_INSTANCE__");

    private StandardContextKeys() {}
}
