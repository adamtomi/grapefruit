package grapefruit.command.dispatcher.config;

import java.util.function.Supplier;

public interface MappingBuilder<T> extends ArgumentMappingBuilder<T> {

    void to(T instance);

    void toSupplier(Supplier<T> supplier);

    Named<T> namedAs(String name);

    interface Named<T> extends ArgumentMappingBuilder<T> {}
}
