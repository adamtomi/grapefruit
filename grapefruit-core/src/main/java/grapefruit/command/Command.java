package grapefruit.command;

import grapefruit.command.argument.CommandArgument;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInvocationException;
import grapefruit.command.dispatcher.CommandSpec;

import java.util.List;

public interface Command extends CommandExecutable {

    List<CommandArgument<?>> arguments();

    CommandSpec spec();

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
