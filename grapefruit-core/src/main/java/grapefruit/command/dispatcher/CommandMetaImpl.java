package grapefruit.command.dispatcher;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

record CommandMetaImpl(String route, Optional<String> permission) implements CommandMeta {

    CommandMetaImpl {
        requireNonNull(route, "route cannot be null");
    }
}
