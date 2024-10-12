package grapefruit.command.dispatcher;

/**
 * Execution stage is used to by {@link ExecutionListener listeners}.
 */
public enum ExecutionStage {
    /**
     * At this stage, the command to execute has been found, but
     * user ipnut has not been parsed into command arguments yet.
     */
    PRE_PROCESS,
    /**
     * At this tage, the command instance has been found and user
     * input has been parsed into command arguments, but the command
     * has not been invoked yet.
     */
    PRE_EXECUTION,
    /**
     * At this stage, the command has been found and executed,
     * and the result of this execution (if any) is available
     * in the current {@link CommandContext context}.
     */
    POST_EXECUTION
}
