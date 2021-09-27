package grapefruit.command;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandDispatcher;
import grapefruit.command.parameter.FlagValueSet;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.parameter.modifier.Flag;

public class Test {

    public static void main(String[] args) {
        final Object source = new Object();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        final ParameterMapper<Object, Integer> intMapper = dispatcher.mappers()
                .findMapper(TypeToken.of(Integer.class))
                .orElseThrow();
        dispatcher.mappers().registerNamedMapper("int", intMapper);
        dispatcher.registerCommands(new Commands());
        dispatcher.dispatchCommand(source, "test -t 1,2,3,4,5");
        dispatcher.dispatchCommand(source, "test --test 1 --test 2 --test 3 --test 4 --test 5");
    }

    private static final class Commands implements CommandContainer {

        @CommandDefinition(route = "test")
        public void testMultiFlags(final @Flag(value = "test", shorthand = 't') FlagValueSet<Integer> ints) {
            System.out.println(ints);
        }
    }
}
