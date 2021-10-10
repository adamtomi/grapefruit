package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        final Optional<CommandParameter<S>> parameterOpt = commandContext.find(SUGGEST_ME);
        final Optional<CommandInput> lastInputOpt = commandContext.find(LAST_INPUT);
        if (parameterOpt.isEmpty() || lastInputOpt.isEmpty()) {
            return List.of();
        }

        final List<CommandParameter<S>> parameters = registration.parameters();
        final CommandParameter<S> parameter = parameterOpt.orElseThrow();
        final String currentArg = args.isEmpty()
                ? lastInputOpt.orElseThrow().rawArg().trim()
                : args.remove().rawArg().trim();

        final ParameterMapper<S, ?> mapper = parameter.mapper();
        final AnnotationList modifiers = parameter.modifiers();
        final List<String> suggestions = new ArrayList<>((parameter.isFlag() && commandContext.find(FLAG_NAME_CONSUMED).isEmpty())
                ? List.of()
                : mapper.listSuggestions(commandContext, currentArg, modifiers));

        final Matcher matcher = FlagParameter.FLAG_PATTERN.matcher(currentArg);

        if (matcher.matches() && parameter.isFlag()) {
            final FlagParameter<S> flag = (FlagParameter<S>) parameter;
            final char shorthand = flag.shorthand();
            final List<String> flagSuggestions = new ArrayList<>();
            final List<FlagParameter<S>> allFlags = parameters.stream()
                    .filter(CommandParameter::isFlag)
                    .map(x -> (FlagParameter<S>) x)
                    .toList();

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
        }

        return suggestions;
    }
}
