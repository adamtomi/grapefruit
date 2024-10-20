package grapefruit.command.runtime.generated;

import grapefruit.command.runtime.CommandAction;
import grapefruit.command.runtime.dispatcher.condition.CommandCondition;
import grapefruit.command.runtime.dispatcher.tree.RouteNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

class CommandMirrorImpl implements CommandMirror {
    private final List<RouteNode> route;
    private final List<ArgumentMirror<?>> arguments;
    private final @Nullable String permission;
    private final List<Class<? extends CommandCondition>> conditions;
    private final CommandAction action;

    CommandMirrorImpl(
            List<RouteNode> route,
            List<ArgumentMirror<?>> arguments,
            @Nullable String permission,
            List<Class<? extends CommandCondition>> conditions,
            CommandAction action
    ) {
        this.route = requireNonNull(route, "route cannot be null");
        this.arguments = requireNonNull(arguments, "arguments cannot be null");
        this.permission = permission;
        this.conditions = requireNonNull(conditions, "conditions cannot be null");
        this.action = requireNonNull(action, "action cannot be null");
    }

    @Override
    public List<RouteNode> route() {
        return List.copyOf(this.route);
    }

    @Override
    public List<ArgumentMirror<?>> arguments() {
        return List.copyOf(this.arguments);
    }

    @Override
    public Optional<String> permission() {
        return Optional.ofNullable(this.permission);
    }

    @Override
    public List<Class<? extends CommandCondition>> conditions() {
        return List.copyOf(this.conditions);
    }

    @Override
    public CommandAction action() {
        return this.action;
    }

    @Override
    public String toString() {
        return "CommandMirrorImpl(route=%s, permission=%s, conditions=%s, action=%s)".formatted(
                this.route, this.permission, this.conditions, this.action
        );
    }
}
