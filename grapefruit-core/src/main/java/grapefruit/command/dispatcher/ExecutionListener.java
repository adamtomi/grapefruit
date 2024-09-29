package grapefruit.command.dispatcher;

@FunctionalInterface
public interface ExecutionListener {

    boolean handle(CommandContext context);
}
