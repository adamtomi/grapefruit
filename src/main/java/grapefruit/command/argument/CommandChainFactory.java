package grapefruit.command.argument;

import grapefruit.command.util.key.Key;
import io.leangen.geantyref.TypeToken;

public interface CommandChainFactory<S> {

    CommandChain.LiteralBuilder<S> newChain();

    CommandArgument.Literal.Builder<S> literal(final Key<String> key);

    CommandArgument.Literal.Builder<S> literal(final String name);

    <T> CommandArgument.Required.Builder<S, T> required(final Key<T> key);

    <T> CommandArgument.Required.Builder<S, T> required(final String name, final TypeToken<T> type);

    <T> CommandArgument.Required.Builder<S, T> required(final String name, final Class<T> type);

    CommandArgument.Flag.BoolBuilder<S> boolFlag(final Key<Boolean> key);

    CommandArgument.Flag.BoolBuilder<S> boolFlag(final String name);

    <T> CommandArgument.Flag.ValueBuilder<S, T> valueFlag(final Key<T> key);

    <T> CommandArgument.Flag.ValueBuilder<S, T> valueFlag(final String name, final TypeToken<T> type);

    <T> CommandArgument.Flag.ValueBuilder<S, T> valueFlag(final String name, final Class<T> type);
}
