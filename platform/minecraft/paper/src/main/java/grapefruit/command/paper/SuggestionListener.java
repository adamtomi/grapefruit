package grapefruit.command.paper;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import grapefruit.command.dispatcher.CommandDispatcher;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

final class SuggestionListener<S> implements Listener {
    private final CommandDispatcher<S> dispatcher;
    private final Function<CommandSender, S> sourceConverter;
    private final List<String> recognizedAliases;

    public SuggestionListener(final @NotNull CommandDispatcher<S> dispatcher,
                              final @NotNull Function<CommandSender, S> sourceConverter,
                              final @NotNull List<String> recognizedAliases) {
        this.dispatcher = requireNonNull(dispatcher, "dispatcher cannot be null");
        this.sourceConverter = requireNonNull(sourceConverter, "sourceConverter cannot be null");
        this.recognizedAliases = requireNonNull(recognizedAliases, "recognizedAliases cannot be null");
    }

    @EventHandler
    public void on(final AsyncTabCompleteEvent event) {
        if (event.getBuffer().trim().isEmpty()) {
            return;
        }

        
    }
}
