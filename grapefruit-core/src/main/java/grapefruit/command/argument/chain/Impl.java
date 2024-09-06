package grapefruit.command.argument.chain;

import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;

import java.util.List;

import static grapefruit.command.dispatcher.InternalContextKeys.INPUT;
import static java.util.Objects.requireNonNull;

final class Impl {
    private Impl() {}

    static final class PositionalBinding<T> extends AbstractBoundArgument<T, CommandArgument<T>> implements BoundArgument.Positional<T> {
        PositionalBinding(CommandArgument<T> argument, ArgumentMapper<T> mapper) {
            super(argument, mapper);
        }
    }

    static final class FlagBinding<T> extends AbstractBoundArgument<T, FlagArgument<T>> implements BoundArgument.Flag<T> {
        FlagBinding(FlagArgument<T> argument, ArgumentMapper<T> mapper) {
            super(argument, mapper);
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
        private final ArgumentMapper<T> mapper;

        AbstractBoundArgument(C argument, ArgumentMapper<T> mapper) {
            this.argument = requireNonNull(argument, "argument cannot be null");
            this.mapper = requireNonNull(mapper, "mapper cannot be null");
        }

        @Override
        public C argument() {
            return this.argument;
        }

        @Override
        public ArgumentMapper<T> mapper() {
            return this.mapper;
        }

        @Override
        public void execute(CommandContext context) throws CommandException {
            T value = mapper().tryMap(context, context.require(INPUT));
            context.put(argument().key(), value);
        }
    }
}
