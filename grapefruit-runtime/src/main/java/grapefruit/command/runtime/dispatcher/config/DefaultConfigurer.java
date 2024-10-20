package grapefruit.command.runtime.dispatcher.config;

import grapefruit.command.runtime.argument.mapper.builtin.CharacterArgumentMapper;
import grapefruit.command.runtime.argument.mapper.builtin.UUIDArgumentMapper;
import grapefruit.command.runtime.argument.modifier.builtin.RegexModifier;

import java.util.UUID;

import static grapefruit.command.runtime.argument.mapper.builtin.NumericArgumentMapper.byteMapper;
import static grapefruit.command.runtime.argument.mapper.builtin.NumericArgumentMapper.doubleMapper;
import static grapefruit.command.runtime.argument.mapper.builtin.NumericArgumentMapper.floatMapper;
import static grapefruit.command.runtime.argument.mapper.builtin.NumericArgumentMapper.intMapper;
import static grapefruit.command.runtime.argument.mapper.builtin.NumericArgumentMapper.longMapper;
import static grapefruit.command.runtime.argument.mapper.builtin.NumericArgumentMapper.shortMapper;
import static grapefruit.command.runtime.argument.mapper.builtin.StringArgumentMapper.GREEDY_NAME;
import static grapefruit.command.runtime.argument.mapper.builtin.StringArgumentMapper.QUOTABLE_NAME;
import static grapefruit.command.runtime.argument.mapper.builtin.StringArgumentMapper.greedy;
import static grapefruit.command.runtime.argument.mapper.builtin.StringArgumentMapper.quotable;
import static grapefruit.command.runtime.argument.mapper.builtin.StringArgumentMapper.single;

/**
 * This class provides basic dispatcher configuration.
 */
public final class DefaultConfigurer extends DispatcherConfigurer {
    private static final DefaultConfigurer INSTANCE = new DefaultConfigurer();

    private DefaultConfigurer() {}

    public static DispatcherConfigurer getInstance() {
        return INSTANCE;
    }

    @Override
    public void configure() {
        // Register string mappers
        map(String.class).using(single());
        map(String.class).namedAs(QUOTABLE_NAME).using(quotable());
        map(String.class).namedAs(GREEDY_NAME).using(greedy());

        // Register primitive type mappers
        map(Character.class).using(new CharacterArgumentMapper());
        map(Byte.class).using(byteMapper());
        map(Short.class).using(shortMapper());
        map(Integer.class).using(intMapper());
        map(Long.class).using(longMapper());
        map(Float.class).using(floatMapper());
        map(Double.class).using(doubleMapper());

        // Register custom type mappers
        map(UUID.class).using(new UUIDArgumentMapper());

        // Register modifier factories
        modifierFactories(new RegexModifier.Factory());
    }
}
