package grapefruit.command.parameter.resolver.builtin;

import grapefruit.command.dispatcher.CommandArgument;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.resolver.AbstractParamterResolver;
import grapefruit.command.parameter.resolver.ParameterResolutionException;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanResolver<S> extends AbstractParamterResolver<S, Boolean> {
    private static final Set<String> TRUE_PHRASES = Set.of("true", "t", "yes", "y", "allow", "1");
    private static final Set<String> FALSE_PHRASES = Set.of("false", "f", "no", "n", "deny", "0");
    private static final Set<String> ALL_OPTIONS = Stream.of(TRUE_PHRASES, FALSE_PHRASES).flatMap(Collection::stream)
            .collect(Collectors.toSet());

    public BooleanResolver() {
        super(TypeToken.get(Boolean.class));
    }

    @Override
    public @NotNull Boolean resolve(final @NotNull S source,
                                    final @NotNull Queue<CommandArgument> args,
                                    final @NotNull CommandParameter param) throws ParameterResolutionException {
        final String input = args.element().rawArg().toLowerCase(Locale.ROOT);
        if (TRUE_PHRASES.contains(input)) {
            return true;
        } else if (FALSE_PHRASES.contains(input)) {
            return false;
        }

        throw new ParameterResolutionException(Message.of(
                MessageKeys.INVALID_BOOLEAN_VALUE,
                Template.of("{input}", input),
                Template.of("{options}", ALL_OPTIONS)
        ), param);
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String currentArg,
                                                 final @NotNull CommandParameter param) {
        return Stream.of(TRUE_PHRASES, FALSE_PHRASES)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
