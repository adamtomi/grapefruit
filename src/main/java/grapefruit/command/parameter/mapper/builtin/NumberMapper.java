package grapefruit.command.parameter.mapper.builtin;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.message.Message;
import grapefruit.command.message.MessageKeys;
import grapefruit.command.message.Template;
import grapefruit.command.parameter.mapper.AbstractParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMappingException;
import grapefruit.command.parameter.modifier.Range;
import grapefruit.command.util.AnnotationList;
import grapefruit.command.util.Miscellaneous;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class NumberMapper<S, N extends Number> extends AbstractParameterMapper<S, N> {
    private static final List<String> PREFIXES;
    private final Function<String, N> converter;

    static {
        final List<Integer> prefixes = new ArrayList<>();
        for (int i = -9; i <= 9; i++) {
            // Don't wan't to start a number with '0'
            if (i != 0) {
                prefixes.add(i);
            }
        }

        PREFIXES = prefixes.stream()
                .map(String::valueOf)
                .toList();
    }

    public NumberMapper(final @NotNull TypeToken<N> type, final @NotNull Function<String, N> converter) {
        super(type);
        this.converter = requireNonNull(converter, "converter cannot be null");
    }

    @Override
    public @NotNull N map(final @NotNull CommandContext<S> context,
                          final @NotNull Queue<CommandInput> args,
                          final @NotNull AnnotationList modifiers) throws ParameterMappingException {
        final String input = args.element().rawArg();
        try {
            final N result = this.converter.apply(input);
            final Optional<Range> rangeOpt = modifiers.find(Range.class);
            if (rangeOpt.isPresent()) {
                final Range range = rangeOpt.get();
                final double min = range.min();
                final double max = range.max();
                final double found = result.doubleValue();

                if (found < min || found > max) {
                    throw new ParameterMappingException(Message.of(
                            MessageKeys.NUMBER_OUT_OF_RANGE,
                            Template.of("{input}", input),
                            Template.of("{min}", min),
                            Template.of("{max}", max)
                    ));
                }
            }

            return result;
        } catch (final NumberFormatException ex) {
            throw new ParameterMappingException(Message.of(
                    MessageKeys.INVALID_NUMBER_VALUE,
                    Template.of("{input}", input)
            ));
        }
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                                 final @NotNull String currentArg,
                                                 final @NotNull AnnotationList modifiers) {
        if (currentArg.isEmpty() || currentArg.equals("-")) {
            return PREFIXES;
        } else {
            if (Miscellaneous.isNumber(currentArg)) {
                final double number = Double.parseDouble(currentArg);
                final Optional<Range> rangeOpt = modifiers.find(Range.class);
                final Predicate<Double> isInRange = x -> rangeOpt
                        .map(range -> range.min() <= x && range.max() >= x)
                        .orElse(true);
                if (!isInRange.test(number)) {
                    return List.of();
                }

                final List<String> result = new ArrayList<>();
                final double multipliedBy10 = number * 10;
                if (!isInRange.test(multipliedBy10)) {
                    return List.of();
                }

                for (int i = 0; i < 9; i++) {
                    final double option = multipliedBy10 + i;
                    if (!isInRange.test(option)) {
                        break;
                    }

                    result.add(String.valueOf(option));
                }

                return result;
            }

            return List.of();
        }
    }
}
