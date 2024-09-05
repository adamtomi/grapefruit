package grapefruit.command.argument;

public interface FlagArgument<T> extends CommandArgument<T> {

    char shorthand();

    boolean isPresenceFlag();
}
