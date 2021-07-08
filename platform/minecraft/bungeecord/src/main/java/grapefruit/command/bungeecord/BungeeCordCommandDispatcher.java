package grapefruit.command.bungeecord;

import grapefruit.command.bungeecord.parameter.PlayerParameterResolver;
import grapefruit.command.dispatcher.AbstractCommandDispatcher;
import grapefruit.command.dispatcher.CommandAuthorizer;
import grapefruit.command.dispatcher.CommandDispatcherBuilder;
import grapefruit.command.message.MessageProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class BungeeCordCommandDispatcher<S> extends AbstractCommandDispatcher<S> {
    private final PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
    private final Plugin plugin;
    private final Function<CommandSender, S> sourceConverter;

    private BungeeCordCommandDispatcher(final @NotNull CommandAuthorizer<S> authorizer,
                                        final @NotNull Executor asyncExecutor,
                                        final @NotNull MessageProvider messageProvider,
                                        final @NotNull Plugin plugin,
                                        final @NotNull Function<CommandSender, S> sourceConverter) {
        super(authorizer, asyncExecutor, messageProvider);
        this.plugin = requireNonNull(plugin, "plugin cannot be null");
        this.sourceConverter = requireNonNull(sourceConverter, "sourceConverter cannot be null");
        resolvers().registerResolver(new PlayerParameterResolver<>());
    }

    @Override
    protected void registerTopLevelCommand(final @NotNull String[] aliases) {
        if (aliases.length == 0) {
            throw new IllegalArgumentException("Aliases is empty");
        }

        final String primaryAlias = aliases[0];
        final String[] aliasesCopy = aliases.length > 1
                ? Arrays.copyOfRange(aliases, 1, aliases.length)
                : new String[0];
        this.pluginManager.registerCommand(this.plugin, new CommandWrapper<>(
                this,
                this.sourceConverter,
                primaryAlias,
                aliasesCopy
        ));
    }

    public static <S> @NotNull BungeeCordCommandDispatcherBuilder<S> builder(final @NotNull Plugin plugin) {
        return new BungeeCordCommandDispatcherBuilder<>(plugin);
    }

    public static final class BungeeCordCommandDispatcherBuilder<S>
            extends CommandDispatcherBuilder<S, BungeeCordCommandDispatcher<S>, BungeeCordCommandDispatcherBuilder<S>> {
        private final Plugin plugin;
        private Function<CommandSender, S> sourceConverter;

        private BungeeCordCommandDispatcherBuilder(final @NotNull Plugin plugin) {
            this.plugin = requireNonNull(plugin, "plugin cannot be null");
        }

        @Override
        protected @NotNull BungeeCordCommandDispatcherBuilder<S> self() {
            return this;
        }

        public @NotNull BungeeCordCommandDispatcherBuilder<S> withSourceConverter(final @NotNull Function<CommandSender, S> sourceConverter) {
            this.sourceConverter = requireNonNull(sourceConverter, "sourceConverter cannot be null");
            return self();
        }

        @Override
        protected BungeeCordCommandDispatcher<S> build(final @NotNull CommandAuthorizer<S> authorizer,
                                                       final @NotNull Executor asyncExecutor,
                                                       final @NotNull MessageProvider messageProvider) {
            return new BungeeCordCommandDispatcher<>(
                    authorizer,
                    asyncExecutor,
                    messageProvider,
                    this.plugin,
                    requireNonNull(this.sourceConverter, "sourceConverter cannot be null")
            );
        }
    }
}
