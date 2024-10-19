package grapefruit.command.runtime.generated;

import grapefruit.command.runtime.argument.modifier.ModifierBlueprint;
import grapefruit.command.runtime.util.key.Key;

import java.util.List;

public interface ArgumentMirror<T> {

    String name();

    Key<T> key();

    Key<T> mapperKey();

    List<ModifierBlueprint> modifiers();

    interface Flag<T> extends ArgumentMirror<T> {

        char shorthand();
    }

    static <T> ArgumentMirror<T> required(String name, Key<T> key, Key<T> mapperKey, List<ModifierBlueprint> modifiers) {
        return new ArgumentMirrorImpl<>(name, key, mapperKey, modifiers);
    }

    static <T> ArgumentMirror.Flag<T> flag(String name, char shorthand, Key<T> key, Key<T> mapperKey, List<ModifierBlueprint> modifiers) {
        return new ArgumentMirrorImpl.Flag<>(name, shorthand, key, mapperKey, modifiers);
    }
}
