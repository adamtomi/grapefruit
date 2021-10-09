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

class SuggestionHelper<S> {
    static final String SUGGEST_ME = "__SUGGEST_ME__";

    SuggestionHelper() {}

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> commandContext,
                                                 final @NotNull CommandRegistration<S> registration,
                                                 final @NotNull Queue<CommandInput> args) throws CommandException {
        final Optional<CommandParameter<S>> parameterOpt = commandContext.find(SUGGEST_ME);
        if (parameterOpt.isEmpty()) {
            return List.of();
        }

        final List<CommandParameter<S>> parameters = registration.parameters();
        final CommandParameter<S> parameter = parameterOpt.orElseThrow();
        if (args.isEmpty()) {
            if (parameter.isFlag()) {
                final FlagParameter<S> flag = (FlagParameter<S>) parameter;
                final List<FlagParameter<S>> possibleFlags = parameters.stream()
                        .filter(CommandParameter::isFlag)
                        .map(x -> (FlagParameter<S>) x)
                        .filter(x -> commandContext.find(x.flagName()).isEmpty())
                        .filter(x -> !x.equals(parameter))
                        .filter(x -> x.shorthand() != ' ')
                        .toList();

                return possibleFlags.stream()
                        .map(FlagParameter::shorthand)
                        .map(x -> Miscellaneous.formatFlag(String.valueOf(flag.shorthand())) + x)
                        .toList();
            }
        }

        final String currentArg = args.isEmpty() ? "" : args.remove().rawArg().trim();
        final List<String> suggestions = new ArrayList<>(parameter.mapper().listSuggestions(commandContext, currentArg, parameter.modifiers()));
        final Matcher matcher = FlagParameter.FLAG_PATTERN.matcher(currentArg);

        if (matcher.matches()) {
            final List<String> flagSuggestions = new ArrayList<>();
            final List<FlagParameter<S>> allFlags = parameters.stream()
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

            suggestions.addAll(flagSuggestions);
        }

        return suggestions;
    }
}
