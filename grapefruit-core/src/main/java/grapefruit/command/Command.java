package grapefruit.command;

import grapefruit.command.argument.CommandArgument;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInvocationException;
import grapefruit.command.dispatcher.CommandMeta;

import java.util.List;

public interface Command extends CommandExecutable {

    List<CommandArgument<?>> arguments();

    CommandMeta meta();

    static Command wrap(List<CommandArgument<?>> arguments, CommandMeta meta, CommandExecutable action) {
        return new Command() {
            @Override
            public List<CommandArgument<?>> arguments() {
                return arguments;
            }

            @Override
            public CommandMeta meta() {
                return meta;
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
