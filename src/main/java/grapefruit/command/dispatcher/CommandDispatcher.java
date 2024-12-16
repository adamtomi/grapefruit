package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.CommandModule;
import grapefruit.command.dispatcher.config.DispatcherConfig;

import java.util.Collection;
import java.util.List;

public interface CommandDispatcher<S> {

    default void register(final Collection<CommandModule<S>> commands) {
        commands.forEach(this::register);
    }

    void register(final CommandModule<S> command);

    default void unregister(final Collection<CommandModule<S>> commands) {
        commands.forEach(this::unregister);
    }

    void unregister(final CommandModule<S> command);

    void dispatch(final S source, final String command) throws CommandException;

    List<String> complete(final S source, final String command);

    static <S> CommandDispatcher<S> using(final DispatcherConfig<S> config) {
        return new CommandDispatcherImpl<>(config);
    }
}
