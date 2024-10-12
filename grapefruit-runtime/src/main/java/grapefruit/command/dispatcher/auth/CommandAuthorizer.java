package grapefruit.command.dispatcher.auth;

import grapefruit.command.dispatcher.CommandContext;

/**
 * A command autorizer is responsible for the authorization
 * of the user attempting to perform a given command.
 */
@FunctionalInterface
public interface CommandAuthorizer {
    /* Always allow the user to run the command */
    CommandAuthorizer ALWAYS_ALLOW = (permission, context) -> true;

    /**
     * Checks and returns whether the user is authorized
     * to run the requested command. If true is returned,
     * the execution process will continue, but if false
     * is returned, a new {@link CommandAuthorizationException}
     * will be thrown by the {@link grapefruit.command.dispatcher.CommandDispatcher}
     * which will contain the required permission.
     *
     * @param permission The permission string
     * @param context The current execution context
     * @return Whether the user is authorized to
     * run the command
     */
    boolean authorize(String permission, CommandContext context);
}
