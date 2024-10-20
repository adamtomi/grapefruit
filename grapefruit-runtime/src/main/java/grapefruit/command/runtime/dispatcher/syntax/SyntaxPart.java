package grapefruit.command.runtime.dispatcher.syntax;

import grapefruit.command.runtime.argument.CommandArgument;

/**
 * Provides basic information of a {@link CommandArgument}.
 */
public interface SyntaxPart {

    /**
     * Returns a formatted string representation of the argument
     * which can be displayed to the user.
     *
     * @return The formatted string representation
     */
    String format();

    /**
     * Returns the {@link Kind} of this syntax part instance.
     * This may be relevant when shown to the user.
     *
     * @return The kind of this syntax part instance.
     */
    Kind kind();

    /**
     * Returns whether the {@link CommandArgument}
     * represented by this syntax part instance is optional. This may be
     * relevant when shown to the user.
     *
     * @return Wheteher this part is optional
     */
    default boolean isOptional() {
        return kind().equals(Kind.FLAG); // Command flags are optional by default
    }

    enum Kind {
        /**
         * Represents required non-flag command arguments.
         */
        ARGUMENT,
        /**
         * Represents flag command arguments.
         */
        FLAG
    }
}
