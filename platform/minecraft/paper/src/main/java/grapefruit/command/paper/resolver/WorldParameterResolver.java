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
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class WorldParameterResolver<S> extends AbstractParamterResolver<S, World> {

    public WorldParameterResolver() {
        super(TypeToken.get(World.class));
    }

    @Override
    public @NotNull World resolve(final @NotNull S source,
                                  final @NotNull Queue<CommandArgument> args,
                                  final @NotNull CommandParameter param) throws ParameterResolutionException {
        final String input = args.element().rawArg();
        final Matcher matcher = Miscellaneous.UUID_PATTERN.matcher(input);
        final World world;

        if (matcher.matches()) {
            final UUID worldId = UUID.fromString(input);
            world = Bukkit.getWorld(worldId);
        } else {
            world = Bukkit.getWorld(input);
        }

        if (world == null) {
            throw new ParameterResolutionException(Message.of(
                    PaperMessageKeys.INVALID_WORLD_VALUE,
                    Template.of("name", input)
            ), param);
        }

        return world;
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String currentArg,
                                                 final @NotNull CommandParameter param) {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toList());
    }
}
