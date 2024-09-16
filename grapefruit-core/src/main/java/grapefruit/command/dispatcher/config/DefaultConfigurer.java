package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.mapper.builtin.UUIDArgumentMapper;
import grapefruit.command.argument.modifier.builtin.RegexModifier;

import java.util.UUID;

import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.GREEDY_NAME;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.QUOTABLE_NAME;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.greedy;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.quotable;
import static grapefruit.command.argument.mapper.builtin.StringArgumentMapper.single;

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
        // Configure argument mappers
        map(String.class).using(single());
        map(String.class).namedAs(QUOTABLE_NAME).using(quotable());
        map(String.class).namedAs(GREEDY_NAME).using(greedy());
        map(UUID.class).using(new UUIDArgumentMapper());

        // Register modifier factories
        modifierFactories(new RegexModifier.Factory());
    }
}
