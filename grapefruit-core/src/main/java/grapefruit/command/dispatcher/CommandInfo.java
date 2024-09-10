package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.argument.binding.BoundArgument;
import grapefruit.command.dispatcher.condition.CommandCondition;

import java.util.List;

import static java.util.Objects.requireNonNull;

record CommandInfo(Command command, List<BoundArgument<?>> arguments, List<BoundArgument<?>> flags, List<CommandCondition> conditions) {
    CommandInfo {
        requireNonNull(command, "command cannot be null");
        requireNonNull(arguments, "arguments cannot be null");
        requireNonNull(flags, "flags cannot be null");
        requireNonNull(conditions, "conditions cannot be null");
    }
}
