package grapefruit.command.argument.mapper.builtin;

import grapefruit.command.CommandException;
import grapefruit.command.argument.mapper.ArgumentMapper;
import grapefruit.command.dispatcher.CommandContext;
import grapefruit.command.dispatcher.input.StringReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class CommaSeparatedArgumentMapper<T, C extends Collection<T>> implements ArgumentMapper<C> {
    private static final char COMMA_CH = ',';
    private static final String COMMA = String.valueOf(COMMA_CH);
    private final ArgumentMapper<T> delegate;
    private final Supplier<C> containerGenerator;

    private CommaSeparatedArgumentMapper(
            ArgumentMapper<T> delegate,
            Supplier<C> containerGenerator
    ) {
        this.delegate = requireNonNull(delegate, "delegate cannot be null");
        this.containerGenerator = requireNonNull(containerGenerator, "containerGenerator cannot be null");
    }

    public static <T, C extends Collection<T>> ArgumentMapper<C> wrap(ArgumentMapper<T> delegate, Supplier<C> containerGenerator) {
        return new CommaSeparatedArgumentMapper<>(delegate, containerGenerator);
    }

    public static <T> ArgumentMapper<Set<T>> set(ArgumentMapper<T> delegate) {
        return new CommaSeparatedArgumentMapper<>(delegate, HashSet::new);
    }

    public static <T> ArgumentMapper<List<T>> list(ArgumentMapper<T> delegate) {
        return new CommaSeparatedArgumentMapper<>(delegate, ArrayList::new);
    }

    @Override
    public C tryMap(CommandContext context, StringReader input) throws CommandException {
        C container = this.containerGenerator.get();
        String[] split = input.readSingle().split(COMMA);
        for (String each : split) {
            if (each.isBlank()) continue;
            T mappedValue = this.delegate.tryMap(context, input);

            container.add(mappedValue);
        }

        return container;
    }

    @Override
    public List<String> complete(CommandContext context, String input) {
        String[] split = input.split(COMMA);
        int index = Math.max(0, split.length - 1);
        String lastArg = split[index];

        List<String> delegateCompletions = this.delegate.complete(context, lastArg);
        if (delegateCompletions.isEmpty()) {
            return List.of();
        } else if (delegateCompletions.contains(lastArg)) {
            return List.of(input + COMMA_CH);
        }

        return delegateCompletions.stream()
                .map(x -> input + x)
                .toList();
    }
}
