package grapefruit.command.argument;

import java.util.List;

public interface CommandChain<S> {

    List<CommandArgument.Literal> route();

    List<CommandArgument.Required<S, ?>> arguments();

    List<CommandArgument.Flag<S, ?>> flags();

    static <S> CommandChainFactory<S> factory() {
        return new CommandChainFactoryImpl<>();
    }

    interface Builder<S, T, B extends Builder<S, T, B>> {

        B then(final T t);

        CommandChain<S> build();
    }

    interface LiteralBuilder<S> extends Builder<S, CommandArgument.Literal, LiteralBuilder<S>> {

        ArgumentBuilder<S> arguments();

        FlagBuilder<S> flags();
    }

    interface ArgumentBuilder<S> extends Builder<S, CommandArgument.Required<S, ?>, ArgumentBuilder<S>> {

        FlagBuilder<S> flags();
    }

    interface FlagBuilder<S> extends Builder<S, CommandArgument.Flag<S, ?>, FlagBuilder<S>> {}
}
