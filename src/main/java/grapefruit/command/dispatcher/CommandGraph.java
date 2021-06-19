package grapefruit.command.dispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

final class CommandGraph {
    private final CommandNode rootNode = new CommandNode("__ROOT__", Set.of(), null);

    public void registerCommand(final @NotNull CommandNode command) {
        registerCommand(this.rootNode, command);
    }

    private void registerCommand(final @NotNull CommandNode parent, final @NotNull CommandNode command) {
        final String primary = command.primary();
        final Optional<CommandNode> registeredChild = parent.findChild(primary);

        registeredChild.ifPresentOrElse(child -> {
            child.mergeAliases(command.aliases());
            command.children().forEach(x -> registerCommand(child, x));

        }, () -> parent.addChild(command));
    }

    public @NotNull Optional<CommandRegistration> routeCommand(final @NotNull String[] args) {
        CommandNode commandNode = this.rootNode;
        for (final String arg : args) {
            Optional<CommandNode> childByName = commandNode.findChild(arg);
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
