package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;

import static grapefruit.command.util.Miscellaneous.containsIgnoreCase;
import static grapefruit.command.util.Miscellaneous.formatFlag;

class SuggestionHelper<S> {
    static final String SUGGEST_ME = "__SUGGEST_ME__";
    static final String LAST_INPUT = "__LAST_INPUT__";
    static final String FLAG_NAME_CONSUMED = "__FLAG_NAME_CONSUMED__";

    SuggestionHelper() {}

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> commandContext,
                                                 final @NotNull CommandRegistration<S> registration,
                                                 final @NotNull Queue<CommandInput> args) {
        System.out.println("sugg helper#listsuggestions");
        final Optional<CommandParameter<S>> parameterOpt = commandContext.find(SUGGEST_ME);
        final Optional<CommandInput> lastInputOpt = commandContext.find(LAST_INPUT);
        if (parameterOpt.isEmpty() || lastInputOpt.isEmpty()) {
            return List.of();
        }

        System.out.println(parameterOpt);
        System.out.println(lastInputOpt);
        System.out.println(commandContext.find(FLAG_NAME_CONSUMED));
        final List<CommandParameter<S>> parameters = registration.parameters();
        final CommandParameter<S> parameter = parameterOpt.orElseThrow();
        final String currentArg = args.isEmpty()
                ? lastInputOpt.orElseThrow().rawArg().trim()
                : args.remove().rawArg().trim();

        System.out.println(parameter);
        System.out.println(currentArg);
        final ParameterMapper<S, ?> mapper = parameter.mapper();
        final AnnotationList modifiers = parameter.modifiers();
        final List<String> suggestions = new ArrayList<>((parameter.isFlag() && commandContext.find(FLAG_NAME_CONSUMED).isEmpty())
                ? List.of()
                : mapper.listSuggestions(commandContext, currentArg, modifiers));

        System.out.println("got suggetions:");
        System.out.println(suggestions);
        final Matcher matcher = FlagParameter.FLAG_PATTERN.matcher(currentArg);
        if (matcher.matches() && parameter.isFlag()) {
            System.out.println("is flag & matches");
            final FlagParameter<S> flag = (FlagParameter<S>) parameter;
            final char shorthand = flag.shorthand();
            final List<String> flagSuggestions = new ArrayList<>();
            final List<FlagParameter<S>> allFlags = parameters.stream()
                    .filter(CommandParameter::isFlag)
                    .map(x -> (FlagParameter<S>) x)
                    .toList();

            System.out.println("flags not seen yet:");
            System.out.println(allFlags.stream().map(FlagParameter::flagName).toList());
            final boolean flagConsumed = commandContext.find(FLAG_NAME_CONSUMED).isPresent();
            for (final FlagParameter<S> each : allFlags) {
                if (commandContext.find(each.flagName()).isPresent()) {
                    continue;
                }

                flagSuggestions.add(formatFlag(each.flagName()));
                final char currentShorthand = each.shorthand();
                if (currentShorthand != ' ') {
                    flagSuggestions.add(formatFlag(String.valueOf(currentShorthand)));
                    if (currentShorthand != shorthand && flagConsumed) {
                        flagSuggestions.add(formatFlag(String.valueOf(shorthand)) + currentShorthand);
                    }
                }
            }

            suggestions.addAll(flagSuggestions);
        } else if (currentArg.startsWith("-")) {
            System.out.println("it's not a flag, adding all possible flags to suggestions");
            final List<FlagParameter<S>> possibleFlags = parameters.stream()
                    .filter(CommandParameter::isFlag)
                    .filter(x -> commandContext.find(x.name()).isEmpty())
                    .map(x -> (FlagParameter<S>) x)
                    .toList();
            possibleFlags.forEach(flag -> {
                suggestions.add(formatFlag(flag.flagName()));
                final char shorthand = flag.shorthand();
                if (shorthand != ' ') {
                    suggestions.add(formatFlag(String.valueOf(shorthand)));
                }
            });
        }

        System.out.println("returning:");
        System.out.println("-----------------------");
        System.out.println(suggestions);
        System.out.println("-----------------------");
        Collections.sort(suggestions);
        return suggestions;
    }
}
