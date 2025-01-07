package grapefruit.command.util.function;

@FunctionalInterface
public interface Function3<A, B, C, D> {

    D apply(final A a, final B b, final C c);
}
