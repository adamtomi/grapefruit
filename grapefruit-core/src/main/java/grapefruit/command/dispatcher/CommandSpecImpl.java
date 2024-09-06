package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.condition.CommandCondition;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

record CommandSpecImpl(String route, Optional<String> permission, List<Class<? extends CommandCondition>> conditions) implements CommandSpec {

    CommandSpecImpl {
        requireNonNull(route, "route cannot be null");
        requireNonNull(permission, "permission cannot be null");
        requireNonNull(conditions, "conditions cannot be null");
    }
}
