package grapefruit.command.runtime.dispatcher.config;

import grapefruit.command.runtime.argument.mapper.ArgumentMapper;
import grapefruit.command.runtime.util.key.Key;
import io.leangen.geantyref.TypeToken;

import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

final class MappingBuilderImpl<T> implements MappingBuilder<T>, MappingBuilder.Named<T> {
    private final TypeToken<T> type;
    private final BiConsumer<Key<T>, ArgumentMapper<T>> handler;
    private String name;

    MappingBuilderImpl(TypeToken<T> type, BiConsumer<Key<T>, ArgumentMapper<T>> handler) {
        this.type = requireNonNull(type, "type cannot be null");
        this.handler = requireNonNull(handler, "handler cannot be null");
    }

    @Override
    public Named<T> namedAs(String name) {
        this.name = requireNonNull(name, "name cannot be null");
        return this;
    }

    @Override
    public void using(ArgumentMapper<T> mapper) {
        Key<T> key = this.name != null
                ? Key.named(this.type, this.name)
                : Key.of(this.type);
        this.handler.accept(key, mapper);
    }
}
