package grapefruit.command;

import grapefruit.command.dispatcher.CommandDispatcher;
import grapefruit.command.parameter.modifier.Flag;
import grapefruit.command.parameter.modifier.string.Quotable;

public class CommandTest {

    public static void main(String[] args) {
        final CommandDispatcher<Object> dispatcher = CommandDispatcher.builder().build();
        dispatcher.registerCommands(new TestCommands());
        final Object source = new Object();
        /*dispatcher.dispatchCommand(source, "test0 first --message 10")
                .thenRun(() -> System.out.println("executed successfully"))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
        dispatcher.dispatchCommand(source, "test1 second --message asd")
                .thenRun(() -> System.out.println("executed successfully"))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
        dispatcher.dispatchCommand(source, "test1 second --message \"this is a quoted phrase\"")
                .thenRun(() -> System.out.println("executed successfully"))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });*/
        dispatcher.dispatchCommand(source, "test2 --test")
                .thenRun(() -> System.out.println("executed successfully"))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public static final class TestCommands implements CommandContainer {

        @CommandDefinition(route = "test0")
        public void onCommand0(final String first, final @Flag("message") int second) {
            System.out.println(first);
            System.out.println(second);
        }

        @CommandDefinition(route = "test1")
        public void onCommand1(final String first, final @Flag("message") @Quotable String phrase) {
            System.out.println(first);
            System.out.println(phrase);
        }

        @CommandDefinition(route = "test2")
        public void onCommand2(final @Flag("test") boolean test) {
            System.out.println(test);
        }
    }
}
