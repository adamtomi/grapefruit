package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

final class CommandGraph<S> {
    private final CommandNode<S> rootNode = new CommandNode<>("__ROOT__", Set.of(), null);

    public void registerCommand(final @NotNull CommandNode<S> command) {
        registerCommand(this.rootNode, command);
    }

    private void registerCommand(final @NotNull CommandNode<S> parent, final @NotNull CommandNode<S> command) {
        final String primary = command.primary();
        final Optional<CommandNode<S>> registeredChild = parent.findChild(primary);

        registeredChild.ifPresentOrElse(child -> {
            child.mergeAliases(command.aliases());
            command.children().forEach(x -> registerCommand(child, x));

        }, () -> parent.addChild(command));
    }

    public @NotNull Optional<CommandRegistration<S>> routeCommand(final @NotNull Queue<String> args) {
        CommandNode<S> commandNode = this.rootNode;
        for (final String arg : args) {
            Optional<CommandNode<S>> childByName = commandNode.findChild(arg);
            if (childByName.isPresent()) {
                commandNode = childByName.get();
            } else {
                childByName = commandNode.children()
                        .stream()
                        .filter(x -> x.aliases().contains(arg))
                        .findFirst();
                if (childByName.isEmpty()) {
                    return Optional.empty();
                }
            }
        }

        return commandNode.registration();
    }

    public @NotNull List<String> listSuggestions(final @NotNull String[] args) {
        return List.of();
    }
}
