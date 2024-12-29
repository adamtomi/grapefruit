package grapefruit.command.dispatcher;

import grapefruit.command.util.ToStringer;

import static java.util.Objects.requireNonNull;

abstract class AbstractCommandResult<S> implements CommandResult<S> {
    private final CommandContext<S> context;
    private final boolean successful;

    AbstractCommandResult(final CommandContext<S> context, final boolean successful) {
        this.context = requireNonNull(context, "context cannot be null");
        this.successful = successful;
    }

    @Override
    public CommandContext<S> context() {
        return this.context;
    }

    @Override
    public boolean successful() {
        return this.successful;
    }

    static final class Failed<S> extends AbstractCommandResult<S> implements CommandResult.Failed<S> {
        private final Throwable exception;

        Failed(final CommandContext<S> context, final Throwable exception) {
            super(context, false);
            this.exception = requireNonNull(exception, "exception cannot be null");
        }

        @Override
        public Throwable exception() {
            return this.exception;
        }

        @Override
        public CommandResult.Failed<S> asFailed() {
            return this;
        }

        @Override
        public CommandResult.Successful<S> asSuccessful() {
            throw new UnsupportedOperationException("Attempting to cast a failed result to a successful result");
        }

        @Override
        public String toString() {
            return ToStringer.create(this)
                    .append("context", context())
                    .append("exception", this.exception)
                    .toString();
        }
    }

    static final class Successful<S> extends AbstractCommandResult<S> implements CommandResult.Successful<S> {
        Successful(final CommandContext<S> context) {
            super(context, true);
        }

        @Override
        public CommandResult.Failed<S> asFailed() {
            throw new UnsupportedOperationException("Attempting to cast a successful result to a failed result");
        }

        @Override
        public CommandResult.Successful<S> asSuccessful() {
            return this;
        }

        @Override
        public String toString() {
            return ToStringer.create(this)
                    .append("context", context())
                    .toString();
        }
    }
}
