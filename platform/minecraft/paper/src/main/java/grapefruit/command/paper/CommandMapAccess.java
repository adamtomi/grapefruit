package grapefruit.command.paper;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Map;

import static java.lang.String.format;

final class CommandMapAccess {
    private static final CommandMap COMMAND_MAP = Bukkit.getCommandMap();
    private static final Constructor<PluginCommand> PLUGIN_COMMAND_CONSTRUCTOR;
    private static final Map<String, Command> KNOWN_COMMANDS = COMMAND_MAP.getKnownCommands();

    static {
        try {
            final Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PLUGIN_COMMAND_CONSTRUCTOR = constructor;

        } catch (final ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    void registerCommand(final @NotNull JavaPlugin plugin,
                         final @NotNull CommandExecutor executor,
                         final @NotNull String[] aliases) {
        for (final String alias : aliases) {
            registerCommand(plugin, executor, alias);
        }
    }

    private void registerCommand(final @NotNull JavaPlugin plugin,
                                 final @NotNull CommandExecutor executor,
                                 final @NotNull String alias) {
        try {
            final PluginCommand pluginCommand = constructCommand(plugin, executor, alias);
            final Command command = COMMAND_MAP.getCommand(alias);
            if (command instanceof PluginCommand foundCommand && foundCommand.getExecutor() instanceof CommandExecutorWrapper) {
                return;
            }

            KNOWN_COMMANDS.put(alias, pluginCommand);
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException(format("Could not register command %s", alias), ex);
        }
    }

    private @NotNull PluginCommand constructCommand(final @NotNull JavaPlugin plugin,
                                                    final @NotNull CommandExecutor executor,
                                                    final @NotNull String alias) throws ReflectiveOperationException {
        final PluginCommand command = PLUGIN_COMMAND_CONSTRUCTOR.newInstance(alias, plugin);
        command.setExecutor(executor);
        return command;
    }
}
