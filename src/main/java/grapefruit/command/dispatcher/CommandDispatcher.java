package grapefruit.command.dispatcher;

import com.google.common.reflect.TypeToken;
import grapefruit.command.CommandContainer;
import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.exception.ExceptionHandler;
import grapefruit.command.dispatcher.listener.PostDispatchListener;
import grapefruit.command.dispatcher.listener.PreDispatchListener;
import grapefruit.command.dispatcher.listener.PreProcessLitener;
import grapefruit.command.parameter.mapper.ParameterMapperRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandDispatcher<S> {

    @NotNull ParameterMapperRegistry<S> mappers();

    void registerListener(final @NotNull PreProcessLitener<S> listener);

    void registerListener(final @NotNull PreDispatchListener<S> listener);

    void registerListener(final @NotNull PostDispatchListener<S> listener);

    <X extends CommandException> void registerHandler(final @NotNull Class<X> clazz,
                                                      final @NotNull ExceptionHandler<S, X> handler);

    void registerCommands(final @NotNull CommandContainer container);

    void dispatchCommand(final @NotNull S source, final @NotNull String commandLine);

    @NotNull List<String> listSuggestions(final @NotNull S source, final @NotNull String commandLine);

    static <S> @NotNull CommandDispatcherBuilder<S> builder(final @NotNull TypeToken<S> commandSourceType) {
        return new CommandDispatcherBuilder<>(commandSourceType);
    }
}
