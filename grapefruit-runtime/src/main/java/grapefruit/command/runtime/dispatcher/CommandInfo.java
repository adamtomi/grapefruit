package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.Command;
import grapefruit.command.runtime.argument.binding.BoundArgument;
import grapefruit.command.runtime.dispatcher.condition.CommandCondition;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Runtime command information gathered and used by {@link CommandDispatcherImpl}.
 */
 @Deprecated
record CommandInfo(Command command, List<BoundArgument<?>> arguments, List<BoundArgument<?>> flags, List<CommandCondition> conditions) {
    CommandInfo {
        requireNonNull(command, "command cannot be null");
        requireNonNull(arguments, "arguments cannot be null");
        requireNonNull(flags, "flags cannot be null");
        requireNonNull(conditions, "conditions cannot be null");
    }
}
