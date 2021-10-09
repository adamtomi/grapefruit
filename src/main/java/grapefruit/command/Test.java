package grapefruit.command;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandDispatcher;
import grapefruit.command.parameter.modifier.Flag;
import grapefruit.command.parameter.modifier.Source;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        final Object source = new Object();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder(TypeToken.of(Object.class))
                .build();
        dispatcher.registerCommands(new Commands());
        final List<String> sugg0 = dispatcher.listSuggestions(source, "test -");
        final List<String> sugg1 = dispatcher.listSuggestions(source, "test -f");
        final List<String> sugg2 = dispatcher.listSuggestions(source, "test --flag 1");
        final List<String> sugg3 = dispatcher.listSuggestions(source, "test 45.3D -opf ");
        System.out.println("=======================");
        System.out.println(sugg0);
        System.out.println(sugg1);
        System.out.println(sugg2);
        System.out.println(sugg3);
    }

    public static final class Commands implements CommandContainer {

        @CommandDefinition(route = "test")
        public void test(final @Source Object source,
                         final @Flag(value = "flag", shorthand = 'f') int i,
                         final @Flag(value = "presence", shorthand = 'p') boolean present,
                         final double d,
                         final @Flag(value = "other", shorthand = 'o') short s) {
            System.out.println("Dispatch!");
            System.out.println(source);
            System.out.println("i: " + i);
            System.out.println("present: " + present);
            System.out.println("d: " + d);
            System.out.println("s: " + s);
        }
    }
}
