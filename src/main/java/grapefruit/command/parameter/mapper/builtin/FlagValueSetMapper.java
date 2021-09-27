package grapefruit.command.parameter.mapper.builtin;

import com.google.common.reflect.TypeToken;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.CommandInput;
import grapefruit.command.dispatcher.StringCommandInput;
import grapefruit.command.parameter.FlagValueSet;
import grapefruit.command.parameter.FlagValueSetImpl;
import grapefruit.command.parameter.mapper.AbstractParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMapper;
import grapefruit.command.parameter.mapper.ParameterMappingException;
import grapefruit.command.util.AnnotationList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.requireNonNull;

public class FlagValueSetMapper<S, T> extends AbstractParameterMapper<S, FlagValueSet<T>> {
    private final TypeToken<T> wrappedType = new TypeToken<>() {};
    private final ParameterMapper<S, T> mapper;

    public FlagValueSetMapper(final @NotNull ParameterMapper<S, T> mapper) {
        super(new TypeToken<>() {});
        this.mapper = requireNonNull(mapper, "mapper cannot be null");
    }

    @Override
    public @NotNull FlagValueSet<T> map(final @NotNull CommandContext<S> context,
                                        final @NotNull Queue<CommandInput> args,
                                        final @NotNull AnnotationList modifiers) throws ParameterMappingException {
        final String[] options = args.element().rawArg().split(",");
        final List<T> result = new ArrayList<>();
        if (options.length == 1) {
            result.add(this.mapper.map(context, args, modifiers));
        } else {
            for (final String option : options) {
                final Queue<CommandInput> arg = new ConcurrentLinkedQueue<>();
                arg.offer(new StringCommandInput(option));
                result.add(this.mapper.map(context, arg, modifiers));
            }
        }

        return new FlagValueSetImpl<>(this.wrappedType, result);
    }

    @Override
    public @NotNull List<String> listSuggestions(final @NotNull CommandContext<S> context,
                                                 final @NotNull String currentArg,
                                                 final @NotNull AnnotationList modifiers) {
        final String[] options = currentArg.split(",");
        return Arrays.stream(options)
                .map(x -> this.mapper.listSuggestions(context, x, modifiers))
                .flatMap(Collection::stream)
                .toList();
    }
}
