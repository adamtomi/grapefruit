package grapefruit.command;

import grapefruit.command.argument.CommandArgument;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInvocationException;
import grapefruit.command.dispatcher.CommandSpec;

import java.util.List;

/**
 * Represents an executable command. A command has a couple of
 * attributes:
 * <ul>
 *     <li>A list of {@link CommandArgument command arguments}</li>
 *     <li>A route at which the command will be available</li>
 *     <li>A permission that is required to execute the command <bold>(Optional)</bold></li>
 *     <li>A list of conditions that first need to pass before executing the actual command <bold>(Optional)</bold></li>
 * </ul>
 *
 * The code generator module will take command methods annotated with {@link grapefruit.command.annotation.CommandDefinition}
 * and through the use of {@link Command#wrap(List, CommandSpec, CommandExecutable)} will generate
 * executable command instances. These commands can be invoked without relying on the use of
 * reflection, increasing performance.
 */
public interface Command extends CommandExecutable {

    /**
     * Returns an immutable list view of the arguments
     * of this command.
     *
     * @return List of arguments
     */
    List<CommandArgument<?>> arguments();

    /**
     * Returns the command spec of this command.
     * @see CommandSpec
     *
     * @return The spec
     */
    CommandSpec spec();

    /**
     * Creates a new command instance wrapping the provided arguments, spec and action. This method
     * is used by the code generator module and is considered an internal API.
     *
     * @param arguments The arguments
     * @param spec The command spec
     * @param action The action to execute
     * @return The created command
     */
    static Command wrap(List<CommandArgument<?>> arguments, CommandSpec spec, CommandExecutable action) {
        return new Command() {
            @Override
            public List<CommandArgument<?>> arguments() {
                return arguments;
            }

            @Override
            public CommandSpec spec() {
                return spec;
            }

            @Override
            public void execute(CommandContext context) throws CommandException {
                try {
                    action.execute(context);
                } catch (CommandException ex) {
                    throw ex;
                } catch (Throwable ex) {
                    throw new CommandInvocationException(ex);
                }
            }
        };
    }
}
