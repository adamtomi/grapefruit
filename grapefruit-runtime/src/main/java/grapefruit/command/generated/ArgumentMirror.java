package grapefruit.command.generated;

import grapefruit.command.util.key.Key;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ArgumentMirror<T> {

    String name();

    Optional<String> description();

    Set<String> permissions();

    Key<T> key();

    Key<T> mapperKey();

    List<Class<?>> modifiers();

    interface Flag<T> extends ArgumentMirror<T> {

        char shorthand();
    }
}
