package grapefruit.command.argument.chain;

import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;

import java.util.List;

import static java.util.Objects.requireNonNull;

final class Impl {
    private Impl() {}

    record FlagBinding<T>(FlagArgument<T> argument, ArgumentMapper<T> mapper) implements BoundArgument.Flag<T> {
        FlagBinding {
            requireNonNull(argument, "argument cannot be null");
            requireNonNull(mapper, "mapper cannot be null");
        }
    }

    record PositionalBinding<T>(CommandArgument<T> argument, ArgumentMapper<T> mapper) implements BoundArgument.Positional<T> {
        PositionalBinding {
            requireNonNull(argument, "argument cannot be null");
            requireNonNull(mapper, "mapper cannot be null");
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
}
