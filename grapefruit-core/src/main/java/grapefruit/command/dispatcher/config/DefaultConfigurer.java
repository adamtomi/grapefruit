package grapefruit.command.dispatcher.config;

import grapefruit.command.annotation.mapper.string.Greedy;
import grapefruit.command.annotation.mapper.string.Quotable;
import grapefruit.command.argument.modifier.standard.RegexModifier;

import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.greedy;
import static grapefruit.command.argument.mapper.standard.StringArgumentMapper.quotable;
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
        map(String.class).namedAs(Quotable.NAME).using(quotable());
        map(String.class).namedAs(Greedy.NAME).using(greedy());

        // Register modifier factories
        modifierFactories(new RegexModifier.Factory());
    }
}
