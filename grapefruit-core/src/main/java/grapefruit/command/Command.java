package grapefruit.command;

import grapefruit.command.argument.CommandArgument;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInvocationException;
import grapefruit.command.dispatcher.CommandMeta;

import java.util.List;

public interface Command extends CommandAction {

    CommandMeta meta();

    default List<CommandArgument<?>> arguments() {
        return meta().arguments();
    }

    static Command wrap(CommandMeta meta, CommandAction action) {
        return new Command() {
            @Override
            public CommandMeta meta() {
                return meta;
            }

            @Override
            public void run(CommandContext context) throws CommandException {
                try {
                    action.run(context);
                } catch (CommandException ex) {
                    throw ex;
                } catch (Throwable ex) {
                    throw new CommandInvocationException(ex);
                }
            }
        };
    }
}
