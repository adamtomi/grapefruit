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
import java.util.function.Predicate;

import static grapefruit.command.util.Miscellaneous.formatFlag;

class SuggestionHelper<S> {
    static final String SUGGEST_ME = "__SUGGEST_ME__";
    static final String LAST_INPUT = "__LAST_INPUT__";
    static final String FLAG_NAME_CONSUMED = "__FLAG_NAME_CONSUMED__";

    SuggestionHelper() {}

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                                 final @NotNull CommandRegistration<S> registration,
                                                 final @NotNull Queue<CommandInput> args) {
        System.out.println("SuggestionHelper#listSuggestions");
        final List<CommandParameter<S>> parameters = registration.parameters();
        final Optional<CommandParameter<S>> parameterOpt = context.<CommandParameter<S>>find(SUGGEST_ME)
                .or(() -> findFirstUnseenParameter(parameters, context));
        final Optional<CommandInput> lastInputOpt = context.find(LAST_INPUT);
        System.out.println(parameterOpt);
        System.out.println(lastInputOpt);
        if (parameterOpt.isEmpty() || lastInputOpt.isEmpty()) {
            return List.of();
        }

        final CommandParameter<S> parameter = parameterOpt.orElseThrow();
        final boolean flagNameConsumed = context.find(FLAG_NAME_CONSUMED).isPresent();
        final boolean isFlag = parameter.isFlag();
        final String currentArg = args.isEmpty()
                ? lastInputOpt.orElseThrow().rawArg().trim()
                : args.remove().rawArg().trim();

        final ParameterMapper<S, ?> mapper = parameter.mapper();
        final AnnotationList modifiers = parameter.modifiers();
        final List<String> suggestions = new ArrayList<>((isFlag && !flagNameConsumed)
                ? List.of()
                : mapper.listSuggestions(context, currentArg, modifiers));

        System.out.println("suggestions so far:");
        System.out.println(suggestions);
        if (!isFlag) {
            System.out.println("not a flag");
            if (currentArg.startsWith("-")) {
                System.out.println("starts with -, adding flags");
                suggestions.addAll(collectUnseenFlagSuggestions(parameters, context));
            }
        } else {
            // Could be that the input is just - or --, in that case our current parameter is the first
            // non-consumed flag
            if (!flagNameConsumed) {
                System.out.println("flag, but the name is not consumed yet");
                suggestions.addAll(collectUnseenFlagSuggestions(parameters, context));
            } else {
                final FlagParameter<S> flag = (FlagParameter<S>) parameter;
                System.out.println("flag && flagNameConsumed, do something here!");

            }
        }

        Collections.sort(suggestions);
        System.out.println("returning:");
        System.out.println(".........................");
        System.out.println(suggestions);
        System.out.println(".........................");
        return suggestions;
    }

    private @NotNull List<String> collectUnseenFlagSuggestions(final @NotNull List<CommandParameter<S>> parameters,
                                                               final @NotNull CommandContext<S> context) {
        return collectUnseenFlagSuggestions(parameters, context, flag -> true);
    }

    private @NotNull List<String> collectUnseenFlagSuggestions(final @NotNull List<CommandParameter<S>> parameters,
                                                               final @NotNull CommandContext<S> context,
                                                               final @NotNull Predicate<FlagParameter<S>> condition) {
        final List<FlagParameter<S>> allFlags = parameters.stream()
                .filter(CommandParameter::isFlag)
                .map(x -> (FlagParameter<S>) x)
                .filter(condition)
                .toList();
        final List<String> result = new ArrayList<>();
        for (final FlagParameter<S> flag : allFlags) {
            final String name = flag.flagName();
            if (context.find(name).isPresent()) {
                continue;
            }

            final char shorthand = flag.shorthand();
            result.add(formatFlag(name));
            if (shorthand != ' ') {
                result.add(formatFlag(shorthand));
            }
        }

        return result;
    }

    private @NotNull Optional<CommandParameter<S>> findFirstUnseenParameter(final @NotNull List<CommandParameter<S>> parameters,
                                                                            final @NotNull CommandContext<S> context) {
        for (final CommandParameter<S> parameter : parameters) {
            final String name = Miscellaneous.parameterName(parameter);
            if (context.find(name).isEmpty()) {
                return Optional.of(parameter);
            }
        }

        return Optional.empty();
    }
}