package grapefruit.command.dispatcher;

@FunctionalInterface
public interface ContextDecorator<S> {

    void apply(final CommandContext<S> context, final Mode mode);

    static <S> ContextDecorator<S> nil() {
        return (context, mode) -> {};
    }

    enum Mode {
        DISPATCH, COMPLETE
    }
}
