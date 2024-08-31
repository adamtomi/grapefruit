package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.input.StringReader;
import grapefruit.command.dispatcher.input.StringReaderImpl;
import grapefruit.command.dispatcher.tree.CommandGraph;

import java.util.List;

public class CommandDispatcherImpl implements CommandDispatcher {
    private final CommandGraph commandGraph = new CommandGraph();

    @Override
    public void register(Iterable<Command> commands) {
        commands.forEach(this.commandGraph::insert);
    }

    @Override
    public void dispatch(CommandContext context, String commandLine) throws CommandException {
        // Construct a new reader from user input
        StringReader input = new StringReaderImpl(commandLine, context);
        // Find the command instance to execute
        Command command = this.commandGraph.search(input);
        // Save the command instance so that we can retrieve it later if needed
        context.store(StandardContextKeys.COMMAND_INSTANCE, command);
    }

    @Override
    public List<String> suggestions(CommandContext context, String commandLine) {
        return List.of();
    }
}
