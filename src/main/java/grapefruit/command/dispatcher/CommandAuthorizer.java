package grapefruit.command.dispatcher;

@FunctionalInterface
public interface CommandAuthorizer<S> {

    boolean authorize(S source, String permission);

    static <S> CommandAuthorizer<S> nil() {
        return (source, perm) -> true;
    };
}
