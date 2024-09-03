package grapefruit.command.dispatcher.config;

import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.greedy;
import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.greedyName;
import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.quotable;
import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.quotableName;
import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.single;

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
        map(String.class).namedAs(quotableName()).using(quotable());
        map(String.class).namedAs(greedyName()).using(greedy());
    }
}
