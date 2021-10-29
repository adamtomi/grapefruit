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
import java.util.regex.Pattern;

import static grapefruit.command.util.Miscellaneous.formatFlag;
import static grapefruit.command.util.Miscellaneous.shorthandNotEmpty;

class SuggestionHelper<S> {
    private static final Pattern FLAG_GROUP_PATTERN = Pattern.compile("^-([a-zA-Z]+)$");

    SuggestionHelper() {}

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                                 final @NotNull CommandRegistration<S> registration,
                                                 final @NotNull Queue<CommandInput> args) {
        final SuggestionContext<S> suggestionContext = context.suggestions();
        final List<CommandParameter<S>> parameters = registration.parameters();
        final Optional<CommandParameter<S>> parameterOpt = suggestionContext.parameter()
                .or(() -> findFirstUnseenParameter(parameters, context));
        final Optional<CommandInput> lastInputOpt = suggestionContext.suggestNext()
                ? Optional.of(new BlankCommandInput(1))
                : suggestionContext.input();

        System.out.println(suggestionContext);
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

        if (!isFlag) {
            if (currentArg.startsWith("-")) {
                suggestions.addAll(collectUnseenFlagSuggestions(parameters, context));
            }
        } else {
            if (!flagNameConsumed) {
                // We don't have a valid flag name at this point, so just return all possible flags
                suggestions.addAll(collectUnseenFlagSuggestions(parameters, context));
            } else {
                final Matcher matcher = FLAG_GROUP_PATTERN.matcher(currentArg);
                if (matcher.matches()) {
                    // So we have a flag group (like -abc). Add all shorthands (if there are any)
                    // The result looks like: [-abcd, -abce]
                    final List<FlagParameter<S>> unseenFlags = collectUnseenFlags(parameters, context);
                    for (final FlagParameter<S> flag : unseenFlags) {
                        final char shorthand = flag.shorthand();
                        if (shorthandNotEmpty(flag) && !currentArg.contains(String.valueOf(shorthand))) {
                            suggestions.add(currentArg + shorthand);
                        }
                    }
                }
            }
        }

        Collections.sort(suggestions);
        return suggestions;
    }

    private @NotNull List<FlagParameter<S>> collectUnseenFlags(final @NotNull List<CommandParameter<S>> parameters,
                                                               final @NotNull CommandContext<S> context) {
        return parameters.stream()
                .filter(CommandParameter::isFlag)
                .map(x -> (FlagParameter<S>) x)
                .filter(x -> context.find(x.flagName()).isEmpty())
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

            result.add(formatFlag(name));
            if (shorthandNotEmpty(flag)) {
                final char shorthand = flag.shorthand();
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
