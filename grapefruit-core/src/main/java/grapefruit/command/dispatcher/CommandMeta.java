package grapefruit.command.dispatcher;

import grapefruit.command.argument.CommandArgument;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface CommandMeta {

    String route();

    Optional<String> permission();

    @Deprecated // TODO this shouldn't be stored on the meta
    List<CommandArgument<?>> arguments();

    static CommandMeta of(String route, @Nullable String permission, List<CommandArgument<?>> arguments) {
        return new CommandMetaImpl(route, Optional.ofNullable(permission), arguments);
    }
}
