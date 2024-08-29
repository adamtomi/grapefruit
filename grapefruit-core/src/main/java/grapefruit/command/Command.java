package grapefruit.command;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInvocationException;
import grapefruit.command.dispatcher.CommandMeta;

public interface Command extends CommandAction {

    CommandMeta meta();

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
