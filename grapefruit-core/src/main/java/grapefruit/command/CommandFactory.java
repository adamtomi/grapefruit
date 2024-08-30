package grapefruit.command;

import java.util.Set;

/**
 * Each class containing at least one method annotated with
 * {@link CommandDefinition} will have a generated _Factory
 * counterpart implementing this interface.
 *
 * @param <T> The original container class
 */
public interface CommandFactory<T> {

    /**
     * Creates an immutable set of every command generated
     * based on command handler methods found in the
     * container class.
     *
     * @param container An instance of the container class
     * @return The set of generated command instances
     */
    Set<Command> generate(T container);
}
