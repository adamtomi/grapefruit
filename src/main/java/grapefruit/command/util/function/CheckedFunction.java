package grapefruit.command.util.function;

@FunctionalInterface
public interface CheckedFunction<A, B, X extends Throwable> {

    B apply(A a) throws X;
}
