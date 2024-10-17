package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.condition.CommandCondition;
import grapefruit.command.dispatcher.tree.RouteNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CommandSpecImpl implements CommandSpec {
    private final List<RouteNode> route;
    private final @Nullable String permission;
    private final List<Class<? extends CommandCondition>> conditions;

    private CommandSpecImpl(List<RouteNode> route, @Nullable String permission, List<Class<? extends CommandCondition>> conditions) {
        this.route = requireNonNull(route, "route cannot be null");
        this.permission = permission; // permission CAN be null
        this.conditions = requireNonNull(conditions, "conditions cannot be null");
    }

    @Override
    public List<RouteNode> route() {
        return this.route;
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
    public String toString() {
        return "CommandSpecImpl(route=%s, permission=%s, conditions=%s)".formatted(this.route, this.permission, this.conditions);
    }

    public static final class Builder implements CommandSpec.Builder {
        private String route;
        private @Nullable String permission;
        private List<Class<? extends CommandCondition>> conditions;

        Builder() {}

        @Override
        public CommandSpec.Builder route(String route) {
            this.route = requireNonNull(route, "route cannot be null");
            return this;
        }

        @Override
        public CommandSpec.Builder permission(@Nullable String permission) {
            this.permission = permission;
            return this;
        }

        @Override
        public CommandSpec.Builder conditions(List<Class<? extends CommandCondition>> conditions) {
            this.conditions = requireNonNull(conditions, "conditions cannot be null");
            return this;
        }

        @Override
        public CommandSpec build() {
            return new CommandSpecImpl(RouteNode.parse(this.route), this.permission, this.conditions);
        }
    }
}
