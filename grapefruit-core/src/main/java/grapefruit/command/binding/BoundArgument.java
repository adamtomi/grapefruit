package grapefruit.command.binding;

import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandException;

public interface BoundArgument<T> {

    T get(CommandContext context) throws CommandException;
}
