package grapefruit.command.bungeecord.parameter;

import grapefruit.command.bungeecord.BungeeMessageKeys;
import grapefruit.command.dispatcher.CommandArgument;
import grapefruit.command.message.Message;
import grapefruit.command.message.Template;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.resolver.AbstractParamterResolver;
import grapefruit.command.parameter.resolver.ParameterResolutionException;
import io.leangen.geantyref.TypeToken;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlayerParameterResolver<S> extends AbstractParamterResolver<S, ProxiedPlayer> {
    private static final Pattern UUID_PATTERN =
            Pattern.compile("([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})");

    public PlayerParameterResolver() {
        super(TypeToken.get(ProxiedPlayer.class));
    }

    @Override
    public @NotNull ProxiedPlayer resolve(final @NotNull S source,
                                          final @NotNull Queue<CommandArgument> args,
                                          final @NotNull CommandParameter param) throws ParameterResolutionException {
        final String input = args.element().rawArg();
        final Matcher matcher = UUID_PATTERN.matcher(input);
        final @Nullable ProxiedPlayer player;
        if (matcher.matches()) {
            final UUID uuid = UUID.fromString(input);
            player = ProxyServer.getInstance().getPlayer(uuid);
        } else {
            player = ProxyServer.getInstance().getPlayer(input);
        }

        if (player == null) {
            throw new ParameterResolutionException(Message.of(
                    BungeeMessageKeys.INVALID_PLAYER_VALUE,
                    Template.of("{input}", input)
            ), param);
        }

        return player;
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String currentArg,
                                                 final @NotNull CommandParameter param) {
        return ProxyServer.getInstance().getPlayers()
                .stream()
                .map(ProxiedPlayer::getName)
                .collect(Collectors.toList());
    }
}
