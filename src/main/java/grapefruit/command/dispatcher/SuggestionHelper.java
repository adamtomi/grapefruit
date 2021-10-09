package grapefruit.command.dispatcher;

import grapefruit.command.dispatcher.registration.CommandRegistration;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.FlagParameter;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static java.util.Objects.requireNonNull;

class SuggestionHelper<S> {
    private final CommandAuthorizer<S> authorizer;

    SuggestionHelper(final @NotNull CommandAuthorizer<S> authorizer) {
        this.authorizer = requireNonNull(authorizer, "authorizer cannot be null");
    }

    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> commandContext,
                                                 final @NotNull String prevArg,
                                                 final @NotNull CommandRegistration<S> registration,
                                                 final @NotNull Queue<CommandInput> args) {
        final List<CommandParameter<S>> parameters = registration.parameters();
        final String currentArg = args.remove().rawArg();

        if (prevArg.startsWith("--")) {
            // TODO cleanup
            final Optional<FlagParameter<S>> flag = parameters.stream()
                    .filter(CommandParameter::isFlag)
                    .map(param -> (FlagParameter<S>) param)
                    .filter(param -> param.flagName().equalsIgnoreCase(prevArg.substring(2)))
                    .filter(param -> commandContext.find(param.flagName()).isEmpty())
                    .findFirst();
            return flag.map(param -> param.mapper().listSuggestions(commandContext, currentArg, param.modifiers()))
                    .orElse(List.of());
        }

        final Optional<CommandParameter<S>> firstNonFlagParameter = parameters.stream()
                .filter(param -> !param.isFlag())
                .filter(param -> commandContext.find(param.name()).isEmpty())
                .findFirst();
        final List<CommandParameter<S>> flags = parameters.stream()
                .filter(CommandParameter::isFlag)
                .filter(param -> commandContext.find(Miscellaneous.parameterName(param)).isEmpty())
                .toList();
        firstNonFlagParameter.ifPresent(flags::add);
        return flags.stream()
                .map(param -> param.mapper().listSuggestions(commandContext, currentArg, param.modifiers()))
                .flatMap(Collection::stream)
                .toList();
    }
}
