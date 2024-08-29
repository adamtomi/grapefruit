package grapefruit.command;

import java.util.Set;

public interface CommandFactory<T> {

    Set<Command> generate(T container);
}
