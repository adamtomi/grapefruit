package grapefruit.command.dispatcher;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface CommandMeta {

    String route();

    Optional<String> permission();

    static CommandMeta of(String route, @Nullable String permission) {
        return new CommandMetaImpl(route, Optional.ofNullable(permission));
    }
}
