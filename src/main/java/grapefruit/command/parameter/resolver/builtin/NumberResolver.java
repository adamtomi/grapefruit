package grapefruit.command.parameter.resolver.builtin;

import grapefruit.command.dispatcher.CommandArg;
import grapefruit.command.parameter.CommandParameter;
import grapefruit.command.parameter.modifier.Range;
import grapefruit.command.parameter.resolver.AbstractParamterResolver;
import grapefruit.command.parameter.resolver.ParameterResolutionException;
import grapefruit.command.util.Miscellaneous;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class NumberResolver<S, N extends Number> extends AbstractParamterResolver<S, N> {
    private static final List<String> PREFIXES;
    private static final List<String> NUMBER_OPTIONS;
    private final Function<String, N> converter;

    static {
        final List<Integer> prefixes = new ArrayList<>();
        for (int i = -9; i < 9; i++) {
            // Don't wan't to start a number with '0'
            if (i != 0) {
                prefixes.add(i);
            }
        }

        PREFIXES = prefixes.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        final List<Integer> numberOptions = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            numberOptions.add(i);
        }

        NUMBER_OPTIONS = numberOptions.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public NumberResolver(final @NotNull TypeToken<N> type, final @NotNull Function<String, N> converter) {
        super(type);
        this.converter = requireNonNull(converter, "converter cannot be null");
    }

    @Override
    public @NotNull N resolve(final @NotNull S source,
                              final @NotNull Queue<CommandArg> args,
                              final @NotNull CommandParameter param) throws ParameterResolutionException {
        final String input = args.element().rawArg();
        try {
            final N result = this.converter.apply(input);
            final Optional<Range> rangeOpt = param.modifiers().find(Range.class);
            if (rangeOpt.isPresent()) {
                final Range range = rangeOpt.get();
                final double min = range.min();
                final double max = range.max();
                final double found = result.doubleValue();

                if (found < min || found > max) {
                    throw new ParameterResolutionException(format("Value has to be between %s and %s", min, max), param);
                }
            }

            return result;
        } catch (final NumberFormatException ex) {
            throw new ParameterResolutionException(format("Invalid number input: %s", input), param);
        }
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull S source,
                                                 final @NotNull String currentArg,
                                                 final @NotNull CommandParameter param) {
        if (currentArg.isEmpty()) {
            return PREFIXES;
        } else {
            if (Miscellaneous.isNumber(currentArg)) {
                final List<String> result = new ArrayList<>();
                for (final String number : NUMBER_OPTIONS) {
                    result.add(currentArg + number);
                }

                return result;
            }

            return List.of();
        }
    }
}
