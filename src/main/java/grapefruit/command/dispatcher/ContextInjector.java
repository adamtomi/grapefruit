package grapefruit.command.dispatcher;

@FunctionalInterface
public interface ContextInjector<S> {

    void injectValues(final CommandContext<S> context, final Mode mode);

    static <S> ContextInjector<S> noop() {
        return (context, mode) -> {};
    }

    enum Mode {
        DISPATCH, COMPLETE
    }
}
