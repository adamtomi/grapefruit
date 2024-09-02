package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.mapper.ArgumentMapper;

/**
 * Used to bind types to {@link ArgumentMapper} instances.
 */
public interface ArgumentMappingBuilder<T> {

    /**
     * Binds the provided mapper to a type.
     *
     * @param mapper The mapper to bind
     */
    void using(ArgumentMapper<T> mapper);
}
