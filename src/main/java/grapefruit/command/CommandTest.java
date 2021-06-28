package grapefruit.command;

import grapefruit.command.dispatcher.CommandDispatcher;
import grapefruit.command.parameter.modifier.Flag;
import grapefruit.command.parameter.modifier.Source;

public class CommandTest {
    public static void main(String[] args) {
        final Object source = new Object();
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder().build();
        dispatcher.registerCommands(new TestCommands());
        //System.out.println(dispatcher.listSuggestions(source, "test0 --message avdddd "));
        //System.out.println(dispatcher.listSuggestions(source, "test1 --tes"));
        System.out.println(dispatcher.listSuggestions(source, "test2 --test asd --test2 1 --test3 4"));
    }

    public static final class TestCommands implements CommandContainer {

        @CommandDefinition(route = "test0")
        public void onCommand0(final @Flag("message") String message, int i) {
            System.out.println(message);
            System.out.println(i);
        }

        @CommandDefinition(route = "test1")
        public void onCommand1(final @Flag("test") boolean test) {
            System.out.println(test);
        }

        @CommandDefinition(route = "test2")
        public void onCommand2(final @Flag("test") String param, @Flag("test2") int param2/*, boolean param3*/, @Flag("test3") double param3) {
            System.out.println(param);
            System.out.println(param2);
            System.out.println(param3);
        }
    }
}
