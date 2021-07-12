package grapefruit.command.paper.resolver;

import grapefruit.command.dispatcher.CommandArgument;
import grapefruit.command.message.Message;
import grapefruit.command.message.Template;
import grapefruit.command.paper.PaperMessageKeys;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.resolver.AbstractParamterResolver;
import grapefruit.command.parameter.resolver.ParameterResolutionException;
import grapefruit.command.util.Miscellaneous;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class PlayerParameterResolver<S> extends AbstractParamterResolver<S, Player> {

    public PlayerParameterResolver() {
        super(TypeToken.get(Player.class));
    }

    @Override
    public @NotNull Player resolve(final @NotNull S source,
                                   final @NotNull Queue<CommandArgument> args,
                                   final @NotNull CommandParameter param) throws ParameterResolutionException {
        final String input = args.element().rawArg();
        final Matcher matcher = Miscellaneous.UUID_PATTERN.matcher(input);
        final Player player;

        if (matcher.matches()) {
            final UUID playerId = UUID.fromString(input);
            player = Bukkit.getPlayer(playerId);
        } else {
            player = Bukkit.getPlayer(input);
        }

        if (player == null) {
            throw new ParameterResolutionException(Message.of(
                    PaperMessageKeys.INVALID_PLAYER_VALUE,
                    Template.of("name", input)
            ), param);
        }

        return player;
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String currentArg,
                                                 final @NotNull CommandParameter param) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
