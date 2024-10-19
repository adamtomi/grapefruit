package grapefruit.command.runtime.generated;

import grapefruit.command.runtime.CommandAction;
import grapefruit.command.runtime.dispatcher.condition.CommandCondition;
import grapefruit.command.runtime.dispatcher.tree.RouteNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface CommandMirror {

    List<RouteNode> route();

    List<ArgumentMirror<?>> arguments();

    Optional<String> permission();

    List<Class<? extends CommandCondition>> conditions();

    CommandAction action();

    static CommandMirror create(
            String route,
            List<ArgumentMirror<?>> arguments,
            @Nullable String permission,
            List<Class<? extends CommandCondition>> conditions,
            CommandAction action
    ) {
        return new CommandMirrorImpl(RouteNode.parse(route), arguments, permission, conditions, action);
    }

    static CommandMirror create(String route, List<ArgumentMirror<?>> arguments, @Nullable String permission, CommandAction action) {
        return create(route, arguments, permission, List.of(), action);
    }
}
