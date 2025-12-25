package grapefruit.command.dispatcher;

import grapefruit.command.argument.CommandChain;
import grapefruit.command.util.function.ToBooleanFunction;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public interface CommandRegistrationHandler<S> {

    boolean register(final CommandChain<S> command);

    boolean unregister(final CommandChain<S> command);

    static <S> CommandRegistrationHandler<S> wrap(
            final @Nullable ToBooleanFunction<CommandChain<S>> registrationFn,
            final @Nullable ToBooleanFunction<CommandChain<S>> unregistrationFn
    ) {
        return new Delegating<>(
                registrationFn == null ? x -> true : registrationFn,
                unregistrationFn == null ? x -> true : unregistrationFn
        );
    }

    final class Delegating<S> implements CommandRegistrationHandler<S> {
        private final ToBooleanFunction<CommandChain<S>> registrationFn;
        private final ToBooleanFunction<CommandChain<S>> unregistrationFn;

        private Delegating(final ToBooleanFunction<CommandChain<S>> registrationFn, final ToBooleanFunction<CommandChain<S>> unregistrationFn) {
            this.registrationFn = requireNonNull(registrationFn, "registrationFn cannot be null");
            this.unregistrationFn = requireNonNull(unregistrationFn, "unregistrationFn cannot be null");
        }

        @Override
        public boolean register(CommandChain<S> command) {
            return this.registrationFn.apply(command);
        }

        @Override
        public boolean unregister(CommandChain<S> command) {
            return this.unregistrationFn.apply(command);
        }
    }
}
