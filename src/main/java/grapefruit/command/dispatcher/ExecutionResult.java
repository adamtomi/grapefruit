package grapefruit.command.dispatcher;

public interface ExecutionResult<S> {

    CommandContext<S> context();

    boolean successful();

    Failed<S> asFailed();

    Successful<S> asSuccessful();

    interface Failed<S> extends ExecutionResult<S> {

        Throwable exception();
    }

    interface Successful<S> extends ExecutionResult<S> {}

    static <S> ExecutionResult<S> successful(final CommandContext<S> context) {
        return new AbstractExecutionResult.Successful<>(context);
    }

    static <S> ExecutionResult<S> failed(final CommandContext<S> context, final Throwable ex) {
        return new AbstractExecutionResult.Failed<>(context, ex);
    }
}
