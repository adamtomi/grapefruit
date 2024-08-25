package grapefruit.command.argument.mapper;

public interface ArgumentMapper<T> {

    T map(String arg);
}
