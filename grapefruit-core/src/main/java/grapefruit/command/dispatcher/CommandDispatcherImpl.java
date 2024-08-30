package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.tree.CommandGraph;

import java.util.List;

public class CommandDispatcherImpl implements CommandDispatcher {
    private final CommandGraph graph = new CommandGraph();

    @Override
    public void register(Iterable<Command> commands) {
        commands.forEach(this.graph::insert);
    }

    @Override
    public void dispatch(CommandContext context, String commandLine) throws CommandException {

    }

    @Override
    public List<String> suggestions(CommandContext context, String commandLine) {
        return List.of();
    }
}
