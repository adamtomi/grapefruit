package grapefruit.command.dispatcher;

import grapefruit.command.CommandException;
import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;

// string(asd) string(test0) string(test1) boolean-flag(b) value-flag(d) value-flag(c)
// input: asd -bcd 100 $
// \_ sugg(d)

// input: asd test0 -b test1 -c 45 $
// \_ sugg(d)

// input: asd -$
// \_ test0 is string -> sugg(-b, -c, -d) # if test0 is number -> sugg(numbers)

// input: asd -b$
// \_ sugg(-bc, -bd)

// input: asd -cd$
// \_ sugg(-cdb)

// input: asd -cd $
// \_ sugg(c) cache(sugg(d))
class SuggestionHelper<S> {
    static final String SUGGEST_ME = "__SUGGEST_ME__";

    SuggestionHelper() {}

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> commandContext,
                                                 final @NotNull CommandRegistration<S> registration,
                                                 final @NotNull Queue<CommandInput> args) throws CommandException {
        System.out.println("SuggestionHelper#listSuggestions");
        final Optional<CommandParameter<S>> parameterOpt = commandContext.find(SUGGEST_ME);
        System.out.println(parameterOpt);
        if (parameterOpt.isEmpty()) {
            return List.of();
        }

        final CommandParameter<S> parameter = parameterOpt.orElseThrow();
        final String currentArg = args.element().rawArg();
        final List<String> suggestions = parameter.mapper().listSuggestions(commandContext, currentArg, parameter.modifiers());
        final Matcher matcher = FlagParameter.FLAG_PATTERN.matcher(currentArg);

        if (matcher.matches()) {
            final List<String> flagSuggestions = new ArrayList<>();
            final List<FlagParameter<S>> allFlags = registration.parameters()
                    .stream()
                    .filter(CommandParameter::isFlag)
                    .map(x -> (FlagParameter<S>) x)
                    .toList();
            for (final FlagParameter<S> each : allFlags) {
                if (commandContext.find(each.flagName()).isPresent()) {
                    continue;
                }

                flagSuggestions.add(Miscellaneous.formatFlag(each.flagName()));
                flagSuggestions.add(Miscellaneous.formatFlag(String.valueOf(each.shorthand())));
            }
            /*for (final CommandParameter<S> each : allFlags) {
                if (!each.isFlag()) {
                    continue;
                }

                final boolean shorthandOnly = matcher.group(1).isEmpty();
                final String nameFragment = matcher.group(2);
                final boolean suggestAllFlags = nameFragment.isEmpty();
                final List<FlagParameter<S>> possibleFlags = allFlags.stream()
                        .filter(CommandParameter::isFlag)
                        .map(x -> (FlagParameter<S>) x)
                        .filter(x -> commandContext.find(x.flagName()).isEmpty())
                        .toList();

                if (shorthandOnly) {
                    if (suggestAllFlags) {
                        possibleFlags.forEach(x -> {
                            flagSuggestions.add(x.flagName());
                            final char shorthand = x.shorthand();
                            if (shorthandNotEmpty(shorthand)) {
                                flagSuggestions.add(String.valueOf(shorthand));
                            }
                        });
                    } else {
                        possibleFlags.forEach(x -> {
                            final char shorthand = x.shorthand();
                            if (shorthandNotEmpty(shorthand) && ) {

                            }
                        });
                    }
                }
                // shorthandonly -> flag names, flag shorthands, flag groups
                // !shorthandonly -> flag names
                suggestions.addAll(flagSuggestions);
            }*/
            suggestions.addAll(flagSuggestions);
        }

        return suggestions;
    }
}
