package grapefruit.command.paper;

import grapefruit.command.dispatcher.AbstractCommandDispatcher;
import grapefruit.command.dispatcher.CommandAuthorizer;
import grapefruit.command.dispatcher.CommandDispatcherBuilder;
import grapefruit.command.message.DefaultMessageProvider;
import grapefruit.command.message.MessageProvider;
import grapefruit.command.paper.resolver.PlayerParameterResolver;
import grapefruit.command.paper.resolver.WorldParameterResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.lang.System.Logger.Level.INFO;
import static java.util.Objects.requireNonNull;

public final class PaperCommandDispatcher<S> extends AbstractCommandDispatcher<S> {
    private static final System.Logger LOGGER = System.getLogger(PaperCommandDispatcher.class.getName());
    private static final boolean ASYNC_SUGGESTIONS_SUPPORT;
    private static final boolean BRIGADIER_SUPPORT;
    private final CommandMapAccess commandMapAccess = new CommandMapAccess();
    private final JavaPlugin plugin;
    private final Function<CommandSender, S> sourceConverter;

    static {
        boolean asyncSuggestions;
        try {
            Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
            asyncSuggestions = true;
        } catch (final ClassNotFoundException ex) {
            asyncSuggestions = false;
        }

        ASYNC_SUGGESTIONS_SUPPORT = asyncSuggestions;

        boolean brigadierSupport;
        try {
            Class.forName("TODO");
            brigadierSupport = true;
        } catch (final ClassNotFoundException ex) {
            brigadierSupport = false;
        }

        BRIGADIER_SUPPORT = brigadierSupport;
    }

    private PaperCommandDispatcher(final @NotNull CommandAuthorizer<S> authorizer,
                                   final @NotNull Executor asyncExecutor,
                                   final @NotNull MessageProvider messageProvider,
                                   final @NotNull JavaPlugin plugin,
                                   final @NotNull Function<CommandSender, S> sourceConverter) {
        super(authorizer, asyncExecutor, messageProvider);
        this.plugin = requireNonNull(plugin, "plugin cannot be null");
        this.sourceConverter = requireNonNull(sourceConverter, "sourceConverter cannot be null");

        if (ASYNC_SUGGESTIONS_SUPPORT) {
            Bukkit.getPluginManager().registerEvents(new SuggestionListener<>(plugin, this, sourceConverter, null), plugin); // TODO
            LOGGER.log(INFO, "Enabled asynchronous suggestion listener");
        }

        resolvers().registerResolver(new PlayerParameterResolver<>());
        resolvers().registerResolver(new WorldParameterResolver<>());

        if (messageProvider instanceof DefaultMessageProvider defaultMessageProvider) {
            registerMessages(defaultMessageProvider);
        }
    }

    private void registerMessages(final @NotNull DefaultMessageProvider messageProvider) {
        messageProvider.register(PaperMessageKeys.INVALID_PLAYER_VALUE, "Player with name '{name}' could not be found");
        messageProvider.register(PaperMessageKeys.INVALID_WORLD_VALUE, "World with name '{name}' could not be found");
    }

    @Override
    protected void registerTopLevelCommand(final @NotNull String[] aliases) {
        final CommandExecutor executor = new CommandExecutorWrapper<>(this, this.sourceConverter);
        this.commandMapAccess.registerCommand(this.plugin, executor, aliases);
    }

    public static <S> @NotNull PaperCommandDispatcherBuilder<S> builder(final @NotNull JavaPlugin plugin) {
        return new PaperCommandDispatcherBuilder<>(plugin);
    }

    public static final class PaperCommandDispatcherBuilder<S> extends CommandDispatcherBuilder<S, PaperCommandDispatcher<S>, PaperCommandDispatcherBuilder<S>> {
        private final JavaPlugin plugin;
        private Function<CommandSender, S> sourceConverter;

        private PaperCommandDispatcherBuilder(final @NotNull JavaPlugin plugin) {
            this.plugin = requireNonNull(plugin, "plugin cannot be null");
        }

        @Override
        protected @NotNull PaperCommandDispatcherBuilder<S> self() {
            return this;
        }

        public @NotNull PaperCommandDispatcherBuilder<S> withSourceConverter(final @NotNull Function<CommandSender, S> sourceConverter) {
            this.sourceConverter = requireNonNull(sourceConverter, "sourceConverter cannot be null");
            return self();
        }

        @Override
        protected PaperCommandDispatcher<S> build(final @NotNull CommandAuthorizer<S> authorizer,
                                                  final @NotNull Executor asyncExecutor,
                                                  final @NotNull MessageProvider messageProvider) {
            return new PaperCommandDispatcher<>(
                    authorizer,
                    asyncExecutor,
                    messageProvider,
                    this.plugin,
                    requireNonNull(this.sourceConverter, "sourceConverter cannot be null")
            );
        }
    }
}
