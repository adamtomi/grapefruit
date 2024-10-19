package grapefruit.command.generated;

import java.util.Set;

/**
 * Every java class containing at least one method annotated with
 * {@link grapefruit.command.annotation.Command} a
 * container class implementing this interface will be generated.
 * This makes it easier to retrieve and use the generated {@link CommandMirror}
 * instances.
 */
public interface CommandContainer {

    /**
     * Creates and returns an immutable {@link Set} of generated
     * {@link CommandMirror} instances, that correspond to methods
     * found in the original container class this container is generated
     * for.
     *
     * @return The generated commands
     */
    Set<CommandMirror> commands();
}
