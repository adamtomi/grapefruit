package grapefruit.command.bungeecord;

import grapefruit.command.dispatcher.CommandDispatcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

final class CommandWrapper<S> extends Command implements TabExecutor {
    private final CommandDispatcher<S> dispatcher;
    private final Function<CommandSender, S> sourceConverter;

    CommandWrapper(final @NotNull CommandDispatcher<S> dispatcher,
                   final @NotNull Function<CommandSender, S> sourceConverter,
                   final @NotNull String name,
                   final @NotNull String[] aliases) {
        super(name, null, aliases);
        this.dispatcher = requireNonNull(dispatcher, "dispatcher cannot be null");
        this.sourceConverter = requireNonNull(sourceConverter, "sourceConverter cannot be null");
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull String[] args) {
        final S source = this.sourceConverter.apply(sender);
        final String commandLine = buildCommandLine(args);
        this.dispatcher.dispatchCommand(source, commandLine);
    }

    @Override
    public @NotNull Iterable<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull String[] args) {
        final S source = this.sourceConverter.apply(sender);
        final String commandLine = buildCommandLine(args);
        return this.dispatcher.listSuggestions(source, commandLine);
    }

    private @NotNull String buildCommandLine(final @NotNull String[] args) {
        final StringJoiner joiner = new StringJoiner(" ");
        joiner.add(getName());
        Arrays.stream(args).forEach(joiner::add);
        return joiner.toString();
    }
}
