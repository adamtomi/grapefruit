package grapefruit.command.dispatcher;

public interface CommandCallable {

    CommandMeta meta();

    void call(CommandContext context) throws CommandException;
}
