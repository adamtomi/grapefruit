package grapefruit.command.dispatcher;

import grapefruit.command.CommandContainer;
import grapefruit.command.dispatcher.listener.PostDispatchListener;
import grapefruit.command.dispatcher.listener.PreDispatchListener;
import grapefruit.command.dispatcher.listener.PreProcessLitener;
import grapefruit.command.message.MessageProvider;
import grapefruit.command.parameter.resolver.ResolverRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandDispatcher<S> {

    @NotNull ResolverRegistry<S> resolvers();

    @NotNull MessageProvider messageProvider();

    void registerListener(final @NotNull PreProcessLitener<S> listener);

    void registerListener(final @NotNull PreDispatchListener<S> listener);

    void registerListener(final @NotNull PostDispatchListener<S> listener);

    void registerCommands(final @NotNull CommandContainer container);

    void dispatchCommand(final @NotNull S source, final @NotNull String commandLine);

    @NotNull List<String> listSuggestions(final @NotNull S source, final @NotNull String commandLine);
}
