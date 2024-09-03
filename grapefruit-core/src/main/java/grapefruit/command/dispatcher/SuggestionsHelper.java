package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is designed to deal with listing suggestions
 * for a command's arguments.
 */
final class SuggestionsHelper {

    SuggestionsHelper() {}

    /**
     * Lists suggestions based on user input, the arguments that already have
     * been parsed and the command instance itself.
     */
    List<String> suggestions(CommandContext context, StringReader input, Command command) {
        /*
         * First, gather a list of arguments that have not been
         * successfully parsed so far.
         */
        List<CommandArgument<?>> unseenArgs = new ArrayList<>();
        // Store flags separately
        List<FlagArgument<?>> unseenFlags = new ArrayList<>();

        // Gather arguments
        for (CommandArgument<?> arg : command.arguments()) {
            // It's been parsed, continue
            if (context.has(arg.key())) continue;

            // If we got to this point, the argument hasn't been parsed yet.
            if (arg.isFlag()) {
                unseenFlags.add((FlagArgument<?>) arg);
            } else {
                unseenArgs.add(arg);
            }
        }

        String arg;
        try {
            arg = input.readRemaining();
        } catch (CommandException ex) {
            // Nothing can be read anymore, we return an empty list.
            return List.of();
        }

        CommandArgument<?> firstArg = !unseenArgs.isEmpty()
                ? unseenArgs.get(0)
                : unseenFlags.get(0);

        return List.of();
    }
}
