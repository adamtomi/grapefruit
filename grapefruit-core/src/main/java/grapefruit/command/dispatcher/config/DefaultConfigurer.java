package grapefruit.command.dispatcher.config;

import grapefruit.command.argument.mapper.standard.StringArgumentMapper;

import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.GREEDY_NAME;
import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.QUOTABLE_NAME;

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
        map(String.class).using(StringArgumentMapper.SINGLE);
        map(String.class).namedAs(QUOTABLE_NAME).using(StringArgumentMapper.QUOTABLE);
        map(String.class).namedAs(GREEDY_NAME).using(StringArgumentMapper.GREEDY);
    }
}
