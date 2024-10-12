package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.mapper.ArgumentMapper;

/**
 * Used to bind types to {@link ArgumentMapper} instances.
 *
 * @param <T> The expected argument type
 */
public interface MappingBuilder<T> {

    /**
     * Binds the provided mapper to a type.
     *
     * @param mapper The mapper to bind
     */
    void using(ArgumentMapper<T> mapper);

    /**
     * Named {@link MappingBuilder} used to set up named mapper
     * bindings.
     *
     * @param <T> THe expected argument type
     */
    interface Named<T> extends MappingBuilder<T> {

        /**
         * Sets up the name for the binding.
         *
         * @param name The name
         * @return The builder
         */
        MappingBuilder<T> namedAs(String name);
    }
}
