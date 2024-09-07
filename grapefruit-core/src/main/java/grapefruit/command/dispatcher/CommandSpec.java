package grapefruit.command.dispatcher;

import grapefruit.command.annotation.CommandDefinition;
import grapefruit.command.dispatcher.condition.CommandCondition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Contains metadata initially held by {@link CommandDefinition}
 * about a specific command.
 */
public interface CommandSpec {

    /**
     * @return The route of the command
     */
    String route();

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
     * Creates a new command spec based on the supplied information.
     *
     * @param route The route
     * @param permission The permission, or null
     * @param conditions The list of conditions
     * @return The created command spec
     */
    @SafeVarargs
    static CommandSpec of(String route, @Nullable String permission, Class<? extends CommandCondition>... conditions) {
        return new CommandSpecImpl(route, Optional.ofNullable(permission), List.of(conditions));
    }
}
