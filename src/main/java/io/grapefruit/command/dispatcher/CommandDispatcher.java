package io.grapefruit.command.dispatcher;

import io.grapefruit.command.CommandContainer;
import io.grapefruit.command.CommandDefinition;
import io.grapefruit.command.CommandException;
import io.grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class CommandDispatcher<S> {
    private static final String ALIAS_SEPARATOR = "\\|";
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final CommandGraph commandGraph = new CommandGraph();
    private final CommandAuthorizer<S> commandAuthorizer;

    private CommandDispatcher(final @NotNull CommandAuthorizer<S> commandAuthorizer) {
        this.commandAuthorizer = requireNonNull(commandAuthorizer);
    }

    public void registerCommands(final @NotNull CommandContainer container) {
        for (final Method method : container.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(CommandDefinition.class)) {
                continue;
            }

            registerCommand(container, method);
        }
    }

    private void registerCommand(final @NotNull CommandContainer container,
                                 final @NotNull Method method) {
        final CommandDefinition def = method.getAnnotation(CommandDefinition.class);
        if (def == null) {
            throw new IllegalStateException(format("Missing @CommandDefinition annotation on %s", method.getName()));
        }

        final List<RouteFragment> route = Arrays.stream(def.route().split(" "))
                .map(String::trim)
                .map(x -> x.split(ALIAS_SEPARATOR))
                .map(x -> {
                    final String primary = x[0];
                    final String[] aliases = x.length > 1
                            ? Arrays.copyOfRange(x, 1, x.length)
                            : new String[0];
                    return new RouteFragment(primary, aliases);
                })
                .collect(Collectors.toList());
        if (route.isEmpty()) {
            throw new IllegalStateException("Empty command tree detected");
        }

        final @Nullable String permission = Miscellaneous.emptyToNull(def.permission());
        final boolean runSync = def.runSync();
        final RouteFragment root = route.remove(0);
        final CommandNode rootNode = new CommandNode(root.primary(), root.aliases(), null);

        try {
            final MethodHandle methodHandle = this.lookup.unreflect(method).bindTo(container);
            final CommandRegistration reg = new CommandRegistration(methodHandle, Set.of(), permission, runSync);

            for (final Iterator<RouteFragment> iter = route.iterator(); iter.hasNext();) {
                final RouteFragment currentFragment = iter.next();
                rootNode.addChild(
                        new CommandNode(currentFragment.primary(), currentFragment.aliases(), iter.hasNext() ? null : reg)
                );
            }

            this.commandGraph.registerCommand(rootNode);
        } catch (final Throwable ex) {
            throw new RuntimeException("Could not register command", ex);
        }
    }

    public void dispatchCommand(final @NotNull S source, final @NotNull String[] args) throws CommandException {
        final Optional<CommandRegistration> registration = this.commandGraph.routeCommand(args);

    }

    public @NotNull List<String> listSuggestions(final @NotNull S sender, final @NotNull String[] args) {
        return List.of();
    }

    private static final record RouteFragment (@NotNull String primary, @NotNull String[] aliases) {}
}
