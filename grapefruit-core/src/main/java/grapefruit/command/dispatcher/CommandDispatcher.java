package grapefruit.command.dispatcher;

public interface CommandDispatcher {

    void register(CommandCallable... commands);

    // TODO proper command source type
    void dispatch(Object source, String commandLine) throws CommandException;
}
