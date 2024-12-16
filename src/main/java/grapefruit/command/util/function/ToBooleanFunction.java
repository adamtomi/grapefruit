package grapefruit.command.util.function;

@FunctionalInterface
public interface ToBooleanFunction<T> {

    boolean apply(T t);
}
