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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static grapefruit.command.util.Miscellaneous.formatFlag;

class SuggestionHelper<S> {
    static final String SUGGEST_ME = "__SUGGEST_ME__";
    static final String LAST_INPUT = "__LAST_INPUT__";
    static final String FLAG_NAME_CONSUMED = "__FLAG_NAME_CONSUMED__";
    private static final Pattern FLAG_GROUP_PATTERN = Pattern.compile("^-([a-zA-Z]+)$");

    SuggestionHelper() {}

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                                 final @NotNull CommandRegistration<S> registration,
                                                 final @NotNull Queue<CommandInput> args) {
        System.out.println("SuggestionHelper#listSuggestions");
        final SuggestionContext<S> suggestionContext = context.suggestions();
        final List<CommandParameter<S>> parameters = registration.parameters();
        final Optional<CommandParameter<S>> parameterOpt = suggestionContext.parameter()
                .or(() -> findFirstUnseenParameter(parameters, context));
        final Optional<CommandInput> lastInputOpt = suggestionContext.input();
        System.out.println(parameterOpt);
        System.out.println(lastInputOpt);
        if (parameterOpt.isEmpty() || lastInputOpt.isEmpty()) {
            return List.of();
        }

        final CommandParameter<S> parameter = parameterOpt.orElseThrow();
        final boolean flagNameConsumed = suggestionContext.flagNameConsumed();
        final boolean isFlag = parameter.isFlag();
        final String currentArg = args.isEmpty()
                ? lastInputOpt.orElseThrow().rawArg().trim()
                : args.remove().rawArg().trim();

        final ParameterMapper<S, ?> mapper = parameter.mapper();
        final AnnotationList modifiers = parameter.modifiers();
        // Lists returned by ParameterMappers may be immutable, so we need to
        // create a mutable copy of the received list
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
            if (!flagNameConsumed) {
                System.out.println("flag, but the name is not consumed yet");
                // We don't have a valid flag name at this point, so just return all possible flags
                suggestions.addAll(collectUnseenFlagSuggestions(parameters, context));
            } else {
                System.out.println("flag && flagNameConsumed, do something here!");
                final Matcher matcher = FLAG_GROUP_PATTERN.matcher(currentArg);
                if (matcher.matches()) {
                    // So we have a flag group (like -abc). Add all shorthands (if there are any)
                    // The result looks like: [-abcd, -abce]
                    System.out.println("looks like this could be a flag group");
                    final List<FlagParameter<S>> unseenFlags = collectUnseenFlags(parameters, context);
                    for (final FlagParameter<S> flag : unseenFlags) {
                        final char shorthand = flag.shorthand();
                        if (shorthandNotEmpty(shorthand) && !currentArg.contains(String.valueOf(shorthand))) {
                            suggestions.add(currentArg + shorthand);
                        }
                    }
                }
            }
        }

        Collections.sort(suggestions);
        System.out.println("returning:");
        System.out.println(".........................");
        System.out.println(suggestions);
        System.out.println(".........................");
        return suggestions;
    }

    private @NotNull List<FlagParameter<S>> collectUnseenFlags(final @NotNull List<CommandParameter<S>> parameters,
                                                               final @NotNull CommandContext<S> context) {
        return parameters.stream()
                .filter(CommandParameter::isFlag)
                .map(x -> (FlagParameter<S>) x)
                .toList();
    }

    private @NotNull List<String> collectUnseenFlagSuggestions(final @NotNull List<CommandParameter<S>> parameters,
                                                               final @NotNull CommandContext<S> context) {
        final List<FlagParameter<S>> allFlags = collectUnseenFlags(parameters, context);
        final List<String> result = new ArrayList<>();
        for (final FlagParameter<S> flag : allFlags) {
            final String name = flag.flagName();
            if (context.find(name).isPresent()) {
                continue;
            }

            final char shorthand = flag.shorthand();
            result.add(formatFlag(name));
            if (shorthandNotEmpty(shorthand)) {
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

    private boolean shorthandNotEmpty(final char shorthand) {
        return shorthand != ' ';
    }
}