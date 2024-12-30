package grapefruit.command.dispatcher;

public interface ExecutionListener<S> {

    @FunctionalInterface
    interface Pre<S> {

        boolean invoke(final CommandContext<S> context);
    }

    @FunctionalInterface
    interface Post<S> {

        void invoke(final ExecutionResult<S> result);
    }
}
