package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.annotation.Command;
import grapefruit.command.runtime.dispatcher.auth.CommandAuthorizer;
import grapefruit.command.runtime.dispatcher.condition.CommandCondition;
import grapefruit.command.runtime.dispatcher.tree.RouteNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Contains metadata initially held by {@link Command}
 * about a specific command.
 */
public interface CommandSpec {

    /**
     * @return The route of the command
     */
    List<RouteNode> route();

    /**
     * @return The permission of the command
     */
    Optional<String> permission();

    /**
     * @return The conditions to test before parsing
     * this command.
     */
    List<Class<? extends CommandCondition>> conditions();

    /**
     * Creates a new builder instance.
     *
     * @return The builder
     */
    static Builder builder() {
        return new CommandSpecImpl.Builder();
    }

    /**
     * Builder to simplify the creation of new {@link CommandSpec} instances.
     * Used by the code generator.
     */
    interface Builder {

        /**
         * Sets the route of the {@link CommandSpec}.
         *
         * @param route The route
         * @return This
         */
        Builder route(String route);

        /**
         * Sets the permission of the {@link CommandSpec}. Passing
         * {@code null} this method means that the {@link CommandAuthorizer}
         * will allow every user to execute the command.
         *
         * @return This
         */
        Builder permission(@Nullable String permission);

        /**
         * Sets the conditions of the {@link CommandSpec}.
         *
         * @see CommandCondition
         * @return This
         */
        Builder conditions(List<Class<? extends CommandCondition>> conditions);

        /**
         * Verifies the provided data and creates a new {@link CommandSpec}.
         *
         * @return The created command spec
         */
        CommandSpec build();
    }
}
