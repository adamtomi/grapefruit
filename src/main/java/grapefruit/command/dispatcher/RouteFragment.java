package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static grapefruit.command.dispatcher.CommandGraph.ALIAS_SEPARATOR;

public record RouteFragment (@NotNull String primary, @NotNull List<String> aliases) {

    static List<RouteFragment> parseRoute(final @NotNull String route) {
        return Arrays.stream(route.split(" "))
                .map(String::trim)
                .map(x -> x.split(ALIAS_SEPARATOR))
                .map(x -> {
                    final String primary = x[0];
                    final String[] aliases = x.length > 1
                            ? Arrays.copyOfRange(x, 1, x.length)
                            : new String[0];
                    return new RouteFragment(primary, List.of(aliases));
                }).toList();
    }
}
