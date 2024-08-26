package grapefruit.command.dispatcher;

import grapefruit.command.argument.CommandArgument;

import java.util.List;

public interface CommandMeta {

    String route();

    List<CommandArgument> arguments();

    static CommandMeta of(String route, List<CommandArgument> arguments) {
        return new CommandMetaImpl(route, arguments);
    }
}
