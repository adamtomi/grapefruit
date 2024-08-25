package grapefruit.command.dispatcher;

public interface CommandCallable {

    void call(CommandContext context) throws CommandException;
}
