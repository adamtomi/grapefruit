package grapefruit.command.runtime.dispatcher;

import grapefruit.command.runtime.dispatcher.config.DispatcherConfigurer;

/**
 * Execution listeners are invoked by the {@link CommandDispatcher}
 * at various stages of command execution. The exact {@link ExecutionStage}
 * is set when registering the listener in {@link DispatcherConfigurer}.
 */
@FunctionalInterface
public interface ExecutionListener {

    /**
     * An execution listener may decide to interrupt command
     * execution, by returning false in this method. Called
     * by the {@link CommandDispatcher} at the appropriate
     * stage.
     *
     * @param context The current context
     * @return Whether to proceed with command execution
     */
    boolean handle(CommandContext context);
}
