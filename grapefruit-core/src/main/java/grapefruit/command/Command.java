package grapefruit.command.dispatcher;

public interface Command {

    CommandMeta meta();

    void run(CommandContext context) throws CommandException;
}
