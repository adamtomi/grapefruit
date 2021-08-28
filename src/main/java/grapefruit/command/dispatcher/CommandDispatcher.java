package grapefruit.command.dispatcher;

import grapefruit.command.CommandContainer;
import grapefruit.command.dispatcher.exception.CommandExceptionHandler;
import grapefruit.command.dispatcher.listener.PostDispatchListener;
import grapefruit.command.dispatcher.listener.PreDispatchListener;
import grapefruit.command.dispatcher.listener.PreProcessLitener;
import grapefruit.command.message.MessageProvider;
import grapefruit.command.parameter.resolver.ResolverRegistry;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandDispatcher<S> {

    @NotNull TypeToken<S> commandSourceType();

    @NotNull ResolverRegistry<S> resolvers();

    @NotNull MessageProvider messageProvider();

    void registerListener(final @NotNull PreProcessLitener<S> listener);

    void registerListener(final @NotNull PreDispatchListener<S> listener);

    void registerListener(final @NotNull PostDispatchListener<S> listener);

    <X extends Throwable> void handle(final @NotNull Class<X> clazz,
                                      final @NotNull CommandExceptionHandler<X, S> handler);

    void registerCommands(final @NotNull CommandContainer container);

    void dispatchCommand(final @NotNull S source, final @NotNull String commandLine);

    @NotNull List<String> listSuggestions(final @NotNull S source, final @NotNull String commandLine);

    static <S> @NotNull CommandDispatcherBuilder<S> builder(final @NotNull TypeToken<S> commandSourceType) {
        return new CommandDispatcherBuilder<>(commandSourceType);
    }
}
