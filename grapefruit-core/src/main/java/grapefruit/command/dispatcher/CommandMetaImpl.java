package grapefruit.command.dispatcher;

import grapefruit.command.argument.CommandArgument;

import java.util.List;

import static java.util.Objects.requireNonNull;

record CommandMetaImpl(String route, List<CommandArgument<?>> arguments) implements CommandMeta {

    CommandMetaImpl {
        requireNonNull(route, "route cannot be null");
        requireNonNull(arguments, "arguments cannot be null");
    }
}
