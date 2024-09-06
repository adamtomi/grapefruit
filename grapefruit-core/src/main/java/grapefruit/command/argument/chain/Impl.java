package grapefruit.command.argument.chain;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.util.ValueFactory;

import java.util.List;

import static java.util.Objects.requireNonNull;

final class Impl {
    private Impl() {}

    static final class PositionalBinding<T> extends AbstractBoundArgument<T, CommandArgument<T>> implements BoundArgument.Positional<T> {
        PositionalBinding(CommandArgument<T> argument, ValueFactory<T> factory) {
            super(argument, factory);
        }
    }

    static final class FlagBinding<T> extends AbstractBoundArgument<T, FlagArgument<T>> implements BoundArgument.Flag<T> {
        FlagBinding(FlagArgument<T> argument, ValueFactory<T> factory) {
            super(argument, factory);
        }
    }

    record ArgumentChainImpl(
            List<BoundArgument.Positional<?>> positional,
            List<BoundArgument.Flag<?>> flag
    ) implements ArgumentChain {
        ArgumentChainImpl {
            requireNonNull(positional, "positionalArguments cannot be null");
            requireNonNull(flag, "flagArguments cannot be null");
        }
    }

    private static abstract class AbstractBoundArgument<T, C extends CommandArgument<T>> implements BoundArgument<T, C> {
        private final C argument;
        private final ValueFactory<T> factory;

        AbstractBoundArgument(C argument, ValueFactory<T> factory) {
            this.argument = requireNonNull(argument, "argument cannot be null");
            this.factory = requireNonNull(factory, "factory cannot be null");
        }

        @Override
        public C argument() {
            return this.argument;
        }

        @Override
        public ValueFactory<T> valueFactory() {
            return this.factory;
        }

        @Override
        public void execute(CommandContext context) throws CommandException {
            T value = valueFactory().compute(context);
            context.put(argument().key(), value);
        }
    }
}
