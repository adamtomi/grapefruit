package grapefruit.command.dispatcher;

public interface CommandResult<S> {

    CommandContext<S> context();

    boolean successful();

    Failed<S> asFailed();

    Successful<S> asSuccessful();

    interface Failed<S> extends CommandResult<S> {

        Throwable exception();
    }

    interface Successful<S> extends CommandResult<S> {}

    static <S> CommandResult<S> successful(final CommandContext<S> context) {
        return new AbstractCommandResult.Successful<>(context);
    }

    static <S> CommandResult<S> failed(final CommandContext<S> context, final Throwable ex) {
        return new AbstractCommandResult.Failed<>(context, ex);
    }
}
