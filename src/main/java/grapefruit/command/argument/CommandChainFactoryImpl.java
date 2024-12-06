package grapefruit.command.argument;

import grapefruit.command.util.key.Key;
import io.leangen.geantyref.TypeToken;

final class CommandChainFactoryImpl<S> implements CommandChainFactory<S> {

    @Override
    public CommandChain.LiteralBuilder<S> newChain() {
        return CommandChainImpl.begin();
    }

    @Override
    public CommandArgument.Literal.Builder literal(final Key<String> key) {
        return new CommandArgumentImpl.LiteralBuilder(key);
    }

    @Override
    public CommandArgument.Literal.Builder literal(final String name) {
        return literal(Key.named(String.class, name));
    }

    @Override
    public <T> CommandArgument.Required.Builder<S, T> required(final Key<T> key) {
        return new CommandArgumentImpl.RequiredBuilder<>(key);
    }

    @Override
    public <T> CommandArgument.Required.Builder<S, T> required(final String name, final TypeToken<T> type) {
        return required(Key.named(type, name));
    }

    @Override
    public <T> CommandArgument.Required.Builder<S, T> required(final String name, final Class<T> type) {
        return required(Key.named(type, name));
    }

    @Override
    public CommandArgument.Flag.PresenceBuilder<S> presenceFlag(final Key<Boolean> key) {
        return new CommandArgumentImpl.PresenceFlagBuilder<>(key);
    }

    @Override
    public CommandArgument.Flag.PresenceBuilder<S> presenceFlag(final String name) {
        return presenceFlag(Key.named(Boolean.class, name));
    }

    @Override
    public <T> CommandArgument.Flag.ValueBuilder<S, T> valueFlag(final Key<T> key) {
        return new CommandArgumentImpl.ValueFlagBuilder<>(key);
    }

    @Override
    public <T> CommandArgument.Flag.ValueBuilder<S, T> valueFlag(final String name, final TypeToken<T> type) {
        return valueFlag(Key.named(type, name));
    }

    @Override
    public <T> CommandArgument.Flag.ValueBuilder<S, T> valueFlag(final String name, final Class<T> type) {
        return valueFlag(Key.named(type, name));
    }
}
