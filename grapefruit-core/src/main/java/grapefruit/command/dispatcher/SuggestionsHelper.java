package grapefruit.command.dispatcher;

import grapefruit.command.Command;
import grapefruit.command.CommandException;
import grapefruit.command.argument.CommandArgument;
import grapefruit.command.argument.FlagArgument;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.input.StringReader;
import grapefruit.command.dispatcher.syntax.CommandSyntax;
import grapefruit.command.util.Registry;
import grapefruit.command.util.key.Key;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static grapefruit.command.dispatcher.syntax.CommandSyntax.LONG_FLAG_PREFIX;
import static grapefruit.command.dispatcher.syntax.CommandSyntax.SHORT_FLAG_PREFIX;
import static java.util.Objects.requireNonNull;

/**
 * This class is designed to deal with listing suggestions.
 */
final class SuggestionsHelper {
    private final Registry<Key<?>, ArgumentMapper<?>> argumentMappers;

    SuggestionsHelper(Registry<Key<?>, ArgumentMapper<?>> argumentMappers) {
        this.argumentMappers = requireNonNull(argumentMappers, "argumentMappers cannot be null");
    }

    /**
     * Lists suggestions based on user input, the arguments that already have
     * been parsed and the command instance itself.
     */
    List<String> suggestions(CommandContext context, StringReader input, Command command) {
        System.out.println("suggesting...");
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

        String str;
        try {
            System.out.println("Attempting to read remaining strings");
            str = input.readRemaining();
        } catch (CommandException ex) {
            System.out.println("Well, this sucks.");
            // Nothing can be read anymore, we return an empty list.
            // return List.of();
            str = "";
        }

        String arg = context.suggestions().lastInput().orElse(str);
        System.out.println("ARG: '%s'".formatted(arg));

        if (arg.isEmpty()) {
            System.out.println("arg is empty,don't suggest anything.");
            // The space hasn't been pressed yet. So the input is something like:
            // 'some route arg0 arg1'. Don't suggest anything.
            return List.of();
        }

        CommandArgument<?> firstArg = !unseenArgs.isEmpty()
                ? unseenArgs.get(0)
                : unseenFlags.get(0);

        CommandArgument<?> argToParse = context.suggestions().lastArgument().orElse(firstArg);

        System.out.println("FirstArg: %s".formatted(argToParse));
        Key<?> key = Key.of(argToParse.key().type());

        if (argToParse.isFlag()) {
            System.out.println("SUGGEST FLAG VALUE: %b".formatted(context.suggestions().suggestFlagValue()));
            if (!context.suggestions().suggestFlagValue()) {
                System.out.println("Don't suggest flag value, returning flag formats");
                return formatFlags(unseenFlags);
            } else {
                System.out.println("Suggest flag values");
                return this.argumentMappers.get(key)
                        .orElseThrow()
                        .listSuggestions(context, arg);
            }
        } else {
            System.out.println("NON-FLAG");
            // Make a mutable copy of the list
            List<String> base = new ArrayList<>(this.argumentMappers.get(key)
                    .orElseThrow()
                    .listSuggestions(context, arg));
            System.out.println("Returning %d elements".formatted(base.size()));

            // If the current argument starts with '-', we list flags as well
            if (arg.startsWith(SHORT_FLAG_PREFIX)) {
                System.out.println("This shit looks like a flag, adding flag suggestions too");
                base.addAll(formatFlags(unseenFlags));
            }

            return base;
        }
    }

    private List<String> formatFlags(Collection<FlagArgument<?>> flags) {
        return flags.stream()
                .map(x -> List.of("%s%s".formatted(SHORT_FLAG_PREFIX, x.shorthand()), "%s%s".formatted(LONG_FLAG_PREFIX, x.name())))
                .flatMap(Collection::stream)
                .toList();
    }
}
