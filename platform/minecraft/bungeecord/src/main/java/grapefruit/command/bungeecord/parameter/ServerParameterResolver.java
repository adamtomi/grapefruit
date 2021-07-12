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
import net.md_5.bungee.api.config.ServerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class ServerParameterResolver<S> extends AbstractParamterResolver<S, ServerInfo> {

    public ServerParameterResolver() {
        super(TypeToken.get(ServerInfo.class));
    }

    @Override
    public @NotNull ServerInfo resolve(final @NotNull S source,
                                       final @NotNull Queue<CommandArgument> args,
                                       final @NotNull CommandParameter param) throws ParameterResolutionException {
        final String input = args.element().rawArg();
        final ServerInfo server = ProxyServer.getInstance().getServerInfo(input);
        if (server == null) {
            throw new ParameterResolutionException(Message.of(
                    BungeeMessageKeys.INVALID_SERVER_VALUE,
                    Template.of("name", input)
            ), param);
        }

        return server;
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String currentArg,
                                                 final @NotNull CommandParameter param) {
        return ProxyServer.getInstance().getServers()
                .values()
                .stream()
                .map(ServerInfo::getName)
                .collect(Collectors.toList());
    }
}
