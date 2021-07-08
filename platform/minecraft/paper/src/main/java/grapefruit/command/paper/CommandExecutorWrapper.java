package grapefruit.command.paper;

import grapefruit.command.dispatcher.CommandDispatcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

final class CommandExecutorWrapper<S> implements TabExecutor {
    private final CommandDispatcher<S> dispatcher;
    private final Function<CommandSender, S> sourceConverter;

    CommandExecutorWrapper(final @NotNull CommandDispatcher<S> dispatcher,
                           final @NotNull Function<CommandSender, S> sourceConverter) {
        this.dispatcher = requireNonNull(dispatcher, "dispatcher cannot be null");
        this.sourceConverter = requireNonNull(sourceConverter, "sourceConverter cannot be null");
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender,
                             final @NotNull Command command,
                             final @NotNull String alias,
                             final @NotNull String[] args) {
        final S source = this.sourceConverter.apply(sender);
        final String commandLine = buildCommandLine(alias, args);
        this.dispatcher.dispatchCommand(source, commandLine);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(final @NotNull CommandSender sender,
                                                final @NotNull Command command,
                                                final @NotNull String alias,
                                                final @NotNull String[] args) {
        final S source = this.sourceConverter.apply(sender);
        final String commandLine = buildCommandLine(alias, args);
        return this.dispatcher.listSuggestions(source, commandLine);
    }

    private @NotNull String buildCommandLine(final @NotNull String alias,
                                             final @NotNull String[] args) {
        final StringJoiner joiner = new StringJoiner(" ");
        joiner.add(alias);
        Arrays.stream(args).forEach(joiner::add);
        return joiner.toString();
    }
}
