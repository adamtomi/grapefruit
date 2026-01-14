package grapefruit.command.util.function;

@FunctionalInterface
public interface CheckedConsumer<T, X extends Throwable> {

    void accept(final T t) throws X;
}
