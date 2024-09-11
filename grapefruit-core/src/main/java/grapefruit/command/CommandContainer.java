package grapefruit.command;

import java.util.Set;

/**
 * Every java class containing at least one method annotated with
 * {@link grapefruit.command.annotation.CommandDefinition} a
 * container class implementing this interface will be generated.
 * This makes it easier to retrieve and use the generated {@link Command}
 * instances.
 */
public interface CommandContainer {

    /**
     * Creates and returns an immutable {@link Set} of generated
     * {@link Command} instances, that correspond to methods
     * found in the original container class this container is generated
     * for.
     *
     * @return The generated commands
     */
    Set<Command> commands();
}
