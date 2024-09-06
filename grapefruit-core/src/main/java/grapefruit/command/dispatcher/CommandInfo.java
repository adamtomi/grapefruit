package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.argument.chain.ArgumentChain;
import grapefruit.command.dispatcher.condition.CommandCondition;

import java.util.List;

import static java.util.Objects.requireNonNull;

record CommandInfo(Command command, ArgumentChain argumentChain, List<CommandCondition> conditions) {
    CommandInfo {
        requireNonNull(command, "command cannot be null");
        requireNonNull(argumentChain, "argumentChain cannot be null");
        requireNonNull(conditions, "conditions cannot be null");
    }
}
